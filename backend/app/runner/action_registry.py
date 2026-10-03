import asyncio
import time
from app.models.scenario import TestAction, TestAssertion
from app.models.execution import StepStatus
from app.adb.client import AdbClient
from app.runner.demo_assertions import DemoShopAssertionProvider

TARGETS = {'ADD_TO_CART': ('ADD_TO_CART', 'product_add_to_cart'),
           'SELECT_UPI': ('SELECT_UPI','checkout_payment_upi'), 'SELECT_CARD': ('SELECT_CARD','checkout_payment_card'),
           'PROCEED_TO_CHECKOUT': ('PROCEED_TO_CHECKOUT', 'cart_checkout'),
           'PAY': ('PAY', 'checkout_pay'), 'PAY \u20b92,499': ('PAY', 'checkout_pay')}

class ActionRegistry:
    def __init__(self, adb_client=None):
        self.adb = adb_client or AdbClient()

    async def execute_step(self, step, device_serial=None, is_mock=False, bridge=None):
        action, target, value = step.action, step.target or '', step.value or ''
        if action == TestAction.CUSTOM:
            return StepStatus.UNSUPPORTED, 'CUSTOM is not allow-listed.', {}
        if is_mock:
            await asyncio.sleep(.3)
            return StepStatus.PASSED, 'Simulated action; no device evidence.', {'mock': True}
        if action == TestAction.TAP and target not in TARGETS:
            return StepStatus.UNSUPPORTED, 'Unknown stable automation target.', {'target': target}
        if action == TestAction.OPEN_SCREEN and target not in ('Product', 'Home', 'Cart', 'Checkout'):
            return StepStatus.UNSUPPORTED, 'Unknown DemoShop screen.', {'target': target}
        try:
            evidence = {'source': 'DEMOSHOP', 'simulated': True, 'network_strategy': 'DEMOSHOP_DEMO_HOOK'}
            if action == TestAction.OPEN_SCREEN:
                state = await asyncio.to_thread(bridge.open_screen, target)
                evidence.update(package='com.pranay.demoshop', screen=target, visible_screen=state['visibleScreen'], verified=True)
            elif action == TestAction.TAP:
                method, tag = TARGETS[target]
                state = await asyncio.to_thread(bridge.call, method)
                if method == 'PAY' and 'PAY_BUTTON_CLICKED' not in state['events']:
                    return StepStatus.FAILED, 'Payment action was not observed.', evidence
                evidence.update(target=tag, verified=True, execution_id=bridge.execution_id)
            elif action == TestAction.CHANGE_NETWORK:
                if value not in ('CELLULAR', 'WIFI_TO_CELLULAR'):
                    return StepStatus.UNSUPPORTED, 'Unsupported demo network transition.', evidence
                state = await asyncio.to_thread(bridge.call, 'ENABLE_DEMO_NETWORK_TRANSITION')
                if not state.get('simulateNetworkTransition'):
                    return StepStatus.FAILED, 'Demo transition was not enabled.', evidence
                evidence.update(verified=True, network_strategy='DEMOSHOP_DEMO_HOOK')
            elif action == TestAction.WAIT:
                ms = int(value)
                if not 0 <= ms <= 10000:
                    return StepStatus.FAILED, 'WAIT must be 0..10000 milliseconds.', {}
                await asyncio.sleep(ms / 1000)
                evidence = {'wait_ms': ms}
            elif action in (TestAction.ASSERT_EVENT, TestAction.ASSERT_API_STATUS, TestAction.ASSERT_VISIBLE, TestAction.ASSERT_TEXT):
                assertion = TestAssertion(type=action.value, target=target, expected=value or 'True')
                return await DemoShopAssertionProvider(bridge).evaluate(assertion)
            elif action == TestAction.BACKGROUND_APP:
                await asyncio.to_thread(self.adb.keyevent, device_serial, 3)
                evidence = {'command_issued': 'HOME'}
            elif action == TestAction.FOREGROUND_APP:
                await asyncio.to_thread(self.adb.start_activity, device_serial, 'com.pranay.demoshop')
                evidence = {'package': 'com.pranay.demoshop', 'command_issued': 'FOREGROUND'}
            elif action == TestAction.ROTATE_DEVICE:
                # Background Activities may defer configuration delivery.
                await asyncio.to_thread(bridge.open_screen, 'Checkout')
                await asyncio.to_thread(self.adb.set_orientation, device_serial, value or 'PORTRAIT')
                deadline=time.monotonic()+3
                while True:
                    state=await asyncio.to_thread(bridge.state)
                    rotated=state.get('checkout',{})
                    if 'CHECKOUT_RECREATED' in state['events'] or time.monotonic()>=deadline:
                        break
                    await asyncio.sleep(.2)
                if rotated.get('orientationBefore') == rotated.get('orientationAfter') or 'CHECKOUT_RECREATED' not in state['events']:
                    return StepStatus.UNSUPPORTED,'No measured checkout configuration change.',{'verified':False}
                evidence = {'orientation':value,'verified':True,'mechanism':'ANDROID_CONFIGURATION','checkout':rotated}
            else:
                return StepStatus.UNSUPPORTED, 'Unsupported action.', {}
            return StepStatus.PASSED, f'Executed {action.value} through the known device operation.', evidence
        except Exception:
            if action == TestAction.ROTATE_DEVICE:
                return StepStatus.UNSUPPORTED,'Actual device rotation unavailable or unverified.',{'verified':False}
            return StepStatus.FAILED, 'Device operation failed. Check DemoShop debug APK and device connection.', {'verified': False}
