from enum import Enum
from pydantic import BaseModel

class DeviceState(str, Enum):
    DEVICE = "device"
    OFFLINE = "offline"
    UNAUTHORIZED = "unauthorized"
    UNKNOWN = "unknown"

class DeviceInfo(BaseModel):
    serial: str
    state: str = "device"
    model: str = "Unknown Model"
    manufacturer: str = "Unknown Manufacturer"
    android_version: str = "14"
