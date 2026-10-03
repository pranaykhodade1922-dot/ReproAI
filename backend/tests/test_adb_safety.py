from unittest.mock import Mock
import pytest
from app.adb.client import AdbClient, AdbException

def test_android_stderr_error_cannot_be_reported_as_success():
    adb = AdbClient()
    adb.run_cmd = Mock(return_value=(0, "Starting: Intent", "Error type 3: Activity does not exist"))
    with pytest.raises(AdbException):
        adb.start_activity("device", "com.pranay.demoshop")

def test_package_target_is_allow_listed():
    adb = AdbClient()
    adb.run_cmd = Mock()
    with pytest.raises(AdbException):
        adb.start_activity("device", "com.untrusted.app")
    with pytest.raises(AdbException):
        adb.force_stop("device", "com.untrusted.app")
    adb.run_cmd.assert_not_called()
