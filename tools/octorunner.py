import subprocess
import time
import os
import sys

class OctoRunner:
    def __init__(self, device_id=None):
        self.device_id = device_id
        self.adb_path = "adb" # Assuming adb is in PATH

    def _run_adb(self, command):
        cmd = [self.adb_path]
        if self.device_id:
            cmd.extend(["-s", self.device_id])
        cmd.extend(command)
        
        print(f"Executing: {' '.join(cmd)}")
        try:
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            return result.stdout.strip()
        except subprocess.CalledProcessError as e:
            print(f"ADB Error: {e.stderr}")
            return None

    def tap(self, x, y):
        """Simulates a tap at (x, y)."""
        return self._run_adb(["shell", "input", "tap", str(x), str(y)])

    def swipe(self, x1, y1, x2, y2, duration=300):
        """Simulates a swipe from (x1, y1) to (x2, y2) with duration in ms."""
        return self._run_adb(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

    def input_text(self, text):
        """Inputs text. Note: spaces might need escaping or special handling."""
        # Replacing spaces with %s is a common ADB hack, but input text is safer
        escaped_text = text.replace(" ", "%s") 
        return self._run_adb(["shell", "input", "text", escaped_text])

    def press_key(self, keycode):
        """Presses a hardware key (e.g., KEYCODE_HOME=3, KEYCODE_BACK=4)."""
        return self._run_adb(["shell", "input", "keyevent", str(keycode)])

    def press_back(self):
        return self.press_key(4)

    def press_home(self):
        return self.press_key(3)

    def take_screenshot(self, local_path):
        """Takes a screenshot and saves it to local_path."""
        remote_path = "/sdcard/screenshot.png"
        self._run_adb(["shell", "screencap", "-p", remote_path])
        self._run_adb(["pull", remote_path, local_path])
        self._run_adb(["shell", "rm", remote_path])
        return local_path

    def dump_ui(self, local_path):
        """Dumps UI hierarchy to XML."""
        remote_path = "/sdcard/window_dump.xml"
        self._run_adb(["shell", "uiautomator", "dump", remote_path])
        self._run_adb(["pull", remote_path, local_path])
        # self._run_adb(["shell", "rm", remote_path]) # Optional cleanup
        return local_path

    def start_app(self, package_name, activity_name):
        """Starts the main activity of the app."""
        return self._run_adb(["shell", "am", "start", "-n", f"{package_name}/{activity_name}"])

    def stop_app(self, package_name):
        """Force stops the app."""
        return self._run_adb(["shell", "am", "force-stop", package_name])

if __name__ == "__main__":
    runner = OctoRunner()
    
    # Example usage (commented out)
    # runner.press_home()
    # runner.take_screenshot("home_screen.png")
    
    if len(sys.argv) > 1:
        cmd = sys.argv[1]
        if cmd == "screenshot":
            runner.take_screenshot("screenshot.png")
            print("Screenshot saved to screenshot.png")
        elif cmd == "dump":
            runner.dump_ui("ui_dump.xml")
            print("UI dump saved to ui_dump.xml")
        elif cmd == "tap" and len(sys.argv) == 4:
            runner.tap(sys.argv[2], sys.argv[3])
