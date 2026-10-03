import logging
from typing import List, Optional
from app.adb.client import AdbClient, AdbException
from app.models.device import DeviceInfo, DeviceState

logger = logging.getLogger("ReproRunner.DeviceManager")

class DeviceManager:

    def __init__(self, adb_client: Optional[AdbClient] = None):
        self.adb = adb_client or AdbClient()

    def get_connected_devices(self) -> List[DeviceInfo]:
        try:
            raw_devices = self.adb.list_devices()
        except AdbException as e:
            logger.warning(f"Could not retrieve device list from ADB: {e}")
            return []

        devices = []
        for serial, state in raw_devices:
            if state != "device":
                continue

            try:
                model = self.adb.get_prop(serial, "ro.product.model") or "Android Device"
                manufacturer = self.adb.get_prop(serial, "ro.product.manufacturer") or "Unknown"
                version = self.adb.get_prop(serial, "ro.build.version.release") or "14"
            except AdbException:
                logger.warning("Device disconnected or became unavailable during discovery")
                continue

            devices.append(
                DeviceInfo(
                    serial=serial,
                    state=DeviceState.DEVICE.value,
                    model=model,
                    manufacturer=manufacturer,
                    android_version=version
                )
            )
        return devices
