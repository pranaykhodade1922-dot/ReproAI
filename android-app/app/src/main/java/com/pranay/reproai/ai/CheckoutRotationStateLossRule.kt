package com.pranay.reproai.ai

import com.pranay.reproai.data.model.DebugEventType
import java.util.UUID

class CheckoutRotationStateLossRule : IncidentAnalysisRule {
    override fun detect(input: AnalysisInput): Boolean {
        val app=input.events.filter {it.metadata["source"]=="DEMOSHOP"}.map {it.title}
        val required=listOf("PAYMENT_METHOD_SELECTED","CHECKOUT_RECREATED","CHECKOUT_STATE_LOST","CHECKOUT_INVALID")
        val positions=required.map {app.indexOf(it)}
        return positions.all {it>=0} && positions.zipWithNext().all {(a,b)->a<b} &&
            input.events.any {it.type==DebugEventType.ORIENTATION_CHANGED && it.metadata["source"] != "DEMOSHOP"}
    }
    override suspend fun analyze(input: AnalysisInput): AnalysisResult {
        val steps=listOf(
            TestActionItem(TestAction.OPEN_SCREEN,"Product",description="Launch DemoShop Product"),
            TestActionItem(TestAction.TAP,"ADD_TO_CART",description="Add product to cart"),
            TestActionItem(TestAction.TAP,"PROCEED_TO_CHECKOUT",description="Proceed to Checkout"),
            TestActionItem(TestAction.TAP,"SELECT_UPI",description="Select UPI and establish valid checkout"),
            TestActionItem(TestAction.ASSERT_EVENT,"CHECKOUT_VALID","True","Confirm valid checkout before rotation"),
            TestActionItem(TestAction.ROTATE_DEVICE,value="LANDSCAPE",description="Rotate the actual device from portrait to landscape"),
            TestActionItem(TestAction.WAIT,value="1000",description="Wait for Activity recreation"))
        val scenario=TestScenario("scen_rotation_${UUID.randomUUID().toString().take(8)}","Checkout Rotation State Loss",
            "Real Android configuration change loses transient checkout selection",
            listOf(TestPrecondition("ORIENTATION","PORTRAIT")),steps,
            listOf("ORIENTATION_CHANGED","CHECKOUT_RECREATED","CHECKOUT_STATE_LOST","CHECKOUT_INVALID","PAYMENT_BLOCKED")
                .map {TestAssertion("ASSERT_EVENT",it,"True")})
        return AnalysisResult("Checkout State Lost After Device Rotation",
            "The selected payment method disappeared after Android recreated Checkout.",
            "Device orientation changed during checkout.",
            "Checkout state was stored only in transient UI state and was not restored after the Android configuration change.",
            94,listOf("Checkout state management","Checkout state holder","Saved state handling"),input.events.map {it.id},
            input.events.map {"${it.type}: ${it.title}"},
            input.events.map {FailureSequenceItem(it.id,it.title,it.metadata["source"] ?: "DEVICE",it.timestamp)},
            steps.mapIndexed {i,s->ReproductionStep(i+1,s.action.name,s.target,s.value,s.description)},
            "Selected payment method should remain available after rotation.",
            "Selected payment method is lost and checkout becomes invalid.",scenario,"RULE_BASED")
    }
}
