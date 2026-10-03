import subprocess
import logging
import re
import time
from typing import List, Optional, Tuple

logger = logging.getLogger("ReproRunner.AdbClient")

class AdbException(Exception):
    pass

class AdbClient:

    def __init__(self, adb_path: str = "adb"):
        from app.config import settings
        self.adb_path = settings.ADB_PATH if adb_path == "adb" else adb_path

    def run_cmd(self, args: List[str], timeout: float = 10.0) -> Tuple[int, str, str]:
        cmd = [self.adb_path] + args
        try:
            result = subprocess.run(
                cmd,
                shell=False,
                capture_output=True,
                text=True,
                timeout=timeout
            )
            return result.returncode, result.stdout.strip(), result.stderr.strip()
        except subprocess.TimeoutExpired:
            logger.error(f"ADB command timed out after {timeout}s: {cmd}")
            raise AdbException(f"ADB command timed out: {cmd}")
        except FileNotFoundError:
            logger.error(f"ADB executable not found at: {self.adb_path}")
            raise AdbException(f"ADB executable not found at: {self.adb_path}")
        except Exception as e:
            logger.error(f"ADB command execution failed: {e}")
            raise AdbException(f"ADB execution failed: {e}")

    def list_devices(self) -> List[Tuple[str, str]]:
        code, stdout, stderr = self.run_cmd(["devices"])
        if code != 0:
            logger.error(f"Failed to list devices: {stderr}")
            return []

        devices = []
        for line in stdout.splitlines()[1:]:
            line = line.strip()
            if not line or "\t" not in line:
                continue
            serial, state = line.split("\t", 1)
            devices.append((serial.strip(), state.strip()))
        return devices

    def shell(self, device_serial: Optional[str], shell_args: List[str]) -> str:
        cmd = []
        if device_serial:
            cmd.extend(["-s", device_serial])
        cmd.append("shell")
        cmd.extend(shell_args)

        code, stdout, stderr = self.run_cmd(cmd)
        if code != 0 or "Error" in stderr or "Exception" in stderr:
            raise AdbException("ADB device command failed. Check device authorization and connection.")
        return stdout

    def get_prop(self, device_serial: Optional[str], prop_name: str) -> str:
        return self.shell(device_serial, ["getprop", prop_name])

    def start_activity(self, device_serial: Optional[str], package_name: str, activity_name: Optional[str] = None) -> bool:
        from app.config import settings
        if package_name not in (settings.DEMOSHOP_PACKAGE, settings.REPROAI_PACKAGE):
            raise AdbException("Package is not allow-listed")
        cmd = ["am", "start"]
        if activity_name:
            cmd.extend(["-n", f"{package_name}/{activity_name}"])
        else:
            cmd.extend(["-n", f"{package_name}/.MainActivity"])

        out = self.shell(device_serial, cmd)
        if "Error" in out or "Exception" in out:
            raise AdbException("Unable to launch the allow-listed app. Check installation.")
        return True

    def force_stop(self, device_serial: Optional[str], package_name: str) -> bool:
        from app.config import settings
        if package_name not in (settings.DEMOSHOP_PACKAGE, settings.REPROAI_PACKAGE):
            raise AdbException("Package is not allow-listed")
        self.shell(device_serial, ["am", "force-stop", package_name])
        return True

    def tap(self, device_serial: Optional[str], x: int, y: int) -> bool:
        self.shell(device_serial, ["input", "tap", str(x), str(y)])
        return True

    def keyevent(self, device_serial: Optional[str], keycode: int) -> bool:
        self.shell(device_serial, ["input", "keyevent", str(keycode)])
        return True

    def set_orientation(self, device_serial: Optional[str], orientation: str) -> bool:
        if orientation.upper() not in ('PORTRAIT','LANDSCAPE'):
            raise AdbException('Unsupported orientation')
        val = "1" if orientation.upper() == "LANDSCAPE" else "0"
        self.shell(device_serial, ["wm", "user-rotation", "lock", val])
        deadline=time.monotonic()+5
        while time.monotonic()<deadline:
            raw=self.shell(device_serial,['dumpsys','input'])
            actual=re.search(r'SurfaceOrientation:\s*(\d)',raw)
            if actual and int(actual.group(1))%2 == int(val):
                time.sleep(.7)
                return True
            time.sleep(.2)
        raise AdbException('Requested device orientation was not observed')

    def get_rotation_lock(self, serial):
        raw=self.shell(serial,['wm','user-rotation']).strip()
        if not re.fullmatch(r'(free|lock) [0-3]',raw):
            raise AdbException('Rotation settings cannot be safely recorded')
        return raw

    def get_surface_orientation(self, serial):
        match = re.search(r'SurfaceOrientation:\s*([0-3])', self.shell(serial, ['dumpsys', 'input']))
        if not match:
            raise AdbException('Actual device orientation cannot be recorded')
        return int(match.group(1))

    def restore_rotation_lock(self, serial, saved):
        if not re.fullmatch(r'(free|lock) [0-3]',saved):
            raise AdbException('Invalid rotation restore state')
        mode,value=saved.split()
        self.shell(serial,['wm','user-rotation','lock',value])
        if mode=='free':
            self.shell(serial,['wm','user-rotation','free'])
        if self.get_rotation_lock(serial)!=saved:
            raise AdbException('Original rotation settings were not restored')
        if mode == 'lock':
            deadline = time.monotonic() + 5
            while time.monotonic() < deadline:
                if self.get_surface_orientation(serial) == int(value):
                    return
                time.sleep(.2)
            raise AdbException('Original display orientation was not restored')
