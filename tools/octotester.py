import os
import sys
import json
import time
import requests
import base64
from octorunner import OctoRunner

# Конфигурация
API_KEYS = [
    "AIzaSyArr9UOyunCdP64Lfn01S6L4v3rS3MzVIw",
    "AIzaSyCkHro9fWyNr9kpqOw1Q9xihPtLiAmkGiE",
    "AIzaSyDa3ycpKRYfm2O-LCOJYm41kyfdCucU3AU"
]
CURRENT_KEY_INDEX = 0

class OctoTester:
    def __init__(self):
        self.runner = OctoRunner()
        self.history = []

    def get_api_key(self):
        return API_KEYS[CURRENT_KEY_INDEX]

    def rotate_key(self):
        global CURRENT_KEY_INDEX
        CURRENT_KEY_INDEX = (CURRENT_KEY_INDEX + 1) % len(API_KEYS)
        print(f"🔄 Switching to API Key #{CURRENT_KEY_INDEX}")

    def call_gemini(self, prompt, image_path=None, ui_xml=None):
        """Отправляет запрос к Gemini Flash для принятия решения."""
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key={self.get_api_key()}"
        
        text_part = {"text": f"System: You are an automated UI tester for an Android app. Your goal is: '{prompt}'.\n"}
        if ui_xml:
            text_part["text"] += f"\nCurrent UI Hierarchy (XML snippet):\n{ui_xml[:2000]}...\n" # Truncate to save tokens
        
        parts = [text_part]

        if image_path and os.path.exists(image_path):
            with open(image_path, "rb") as f:
                img_data = base64.b64encode(f.read()).decode("utf-8")
            parts.append({
                "inline_data": {
                    "mime_type": "image/png",
                    "data": img_data
                }
            })

        parts.append({"text": "Based on the screenshot and UI dump, output a JSON object with the next action. \n" \
                              "Possible actions: \n" \
                              "- {'action': 'tap', 'x': 100, 'y': 200, 'reason': '...'}\n" \
                              "- {'action': 'swipe', 'x1': 500, 'y1': 1000, 'x2': 500, 'y2': 200, 'duration': 300, 'reason': '...'}\n" \
                              "- {'action': 'input', 'text': 'hello', 'reason': '...'}\n" \
                              "- {'action': 'back', 'reason': '...'}\n" \
                              "- {'action': 'finish', 'status': 'success', 'report': '...'}\n" \
                              "- {'action': 'finish', 'status': 'fail', 'report': '...'}\n" \
                              "Output ONLY JSON."})))

        payload = {
            "contents": [{"parts": parts}]
        }

        try:
            response = requests.post(url, json=payload, headers={"Content-Type": "application/json"})
            if response.status_code != 200:
                print(f"⚠️ API Error {response.status_code}: {response.text}")
                self.rotate_key()
                return None
            
            data = response.json()
            if "candidates" in data and data["candidates"]:
                content = data["candidates"][0]["content"]["parts"][0]["text"]
                # Очистка от markdown
                content = content.replace("```json", "").replace("```", "").strip()
                return json.loads(content)
            return None
        except Exception as e:
            print(f"🔥 Exception calling Gemini: {e}")
            return None

    def run_test(self, goal, max_steps=10):
        print(f"🚀 Starting Test: {goal}")
        report = []
        
        for step in range(max_steps):
            print(f"\n--- Step {step + 1} ---")
            
            # 1. Снимаем состояние
            screen_path = "temp_screen.png"
            dump_path = "temp_dump.xml"
            self.runner.take_screenshot(screen_path)
            self.runner.dump_ui(dump_path)
            
            with open(dump_path, "r", encoding="utf-8", errors="ignore") as f:
                xml_content = f.read()

            # 2. Думаем
            decision = self.call_gemini(goal, screen_path, xml_content)
            
            if not decision:
                print("❌ AI failed to decide.")
                break
                
            print(f"🤖 AI Decision: {decision}")
            action = decision.get("action")
            reason = decision.get("reason", "")
            report.append(f"Step {step}: {action} - {reason}")

            # 3. Действуем
            if action == "tap":
                self.runner.tap(decision["x"], decision["y"])
            elif action == "swipe":
                self.runner.swipe(decision["x1"], decision["y1"], decision["x2"], decision["y2"])
            elif action == "input":
                self.runner.input_text(decision["text"])
            elif action == "back":
                self.runner.press_back()
            elif action == "finish":
                print(f"🏁 Test Finished: {decision.get('status')}")
                print(f"📄 Report: {decision.get('report')}")
                return
            
            time.sleep(2) # Ждем анимаций

        print("⚠️ Max steps reached.")

if __name__ == "__main__":
    tester = OctoTester()
    if len(sys.argv) > 1:
        goal = " ".join(sys.argv[1:])
        tester.run_test(goal)
    else:
        # Default test
        tester.run_test("Find the 'Homework' section and open the first homework item.")
