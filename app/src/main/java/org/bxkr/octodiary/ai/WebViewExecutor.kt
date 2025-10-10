package org.bxkr.octodiary.ai

import android.webkit.WebView
import com.google.gson.Gson
import kotlinx.coroutines.delay

data class ActionStep(
    val action: String,
    val target: Map<String, Any>? = null,
    val from: Map<String, Any>? = null,
    val to: Map<String, Any>? = null,
    val text: String? = null,
    val ms: Int? = null
)

data class ActionPlan(
    val meta: Map<String, Any>,
    val steps: List<ActionStep>
)

object WebViewExecutor {
    private val jsHelpers = """
        window.OctoAI = {
            text: function(el) {
                return (el.innerText || el.textContent || '').trim().toLowerCase();
            },
            findByText: function(text) {
                var labels = ['далее','дальше','продолжить','next','continue'];
                var cands = Array.from(document.querySelectorAll('button, a[role="button"], [role="button"], .MuiButtonBase-root, .MuiButton-root, .btn, .Button, label, span, p, div'));
                for(var i=0; i<cands.length; i++) {
                    var el = cands[i];
                    var t = this.text(el);
                    if (t === text.toLowerCase() || t.includes(text.toLowerCase())) {
                        return el;
                    }
                }
                return null;
            },
            findLabelByText: function(text) {
                var labels = Array.from(document.querySelectorAll('label'));
                for(var i=0; i<labels.length; i++) {
                    if (this.text(labels[i]).includes(text.toLowerCase())) {
                        return labels[i];
                    }
                }
                return null;
            },
            findCheckboxByText: function(text) {
                var label = this.findLabelByText(text);
                if (!label) return null;
                var forId = label.getAttribute('for');
                if (forId) {
                    return document.getElementById(forId);
                }
                return label.querySelector('input[type="checkbox"]');
            },
            clickElement: function(el) {
                if (!el) return false;
                el.click();
                return true;
            },
            checkCheckbox: function(el) {
                if (!el) return false;
                if (!el.checked) el.click();
                return true;
            },
            uncheckCheckbox: function(el) {
                if (!el) return false;
                if (el.checked) el.click();
                return true;
            },
            dragDrop: function(fromEl, toEl) {
                if (!fromEl || !toEl) return false;
                var evt = new MouseEvent('mousedown', {bubbles: true, cancelable: true});
                fromEl.dispatchEvent(evt);
                evt = new MouseEvent('mousemove', {bubbles: true, cancelable: true, clientX: toEl.getBoundingClientRect().x, clientY: toEl.getBoundingClientRect().y});
                document.dispatchEvent(evt);
                evt = new MouseEvent('mouseup', {bubbles: true, cancelable: true});
                toEl.dispatchEvent(evt);
                return true;
            },
            typeText: function(el, text) {
                if (!el) return false;
                el.focus();
                el.value = text;
                el.dispatchEvent(new Event('input', {bubbles: true}));
                el.dispatchEvent(new Event('change', {bubbles: true}));
                return true;
            }
        };
    """.trimIndent()

    suspend fun execute(webView: WebView, plan: ActionPlan) {
        // Inject helpers
        webView.evaluateJavascript(jsHelpers, null)
        delay(100)

        for (step in plan.steps) {
            when (step.action) {
                "wait" -> {
                    delay(step.ms?.toLong() ?: 1000)
                }
                "click" -> {
                    val text = step.target?.get("text") as? String
                    if (text != null) {
                        val js = "OctoAI.clickElement(OctoAI.findByText('$text'))"
                        webView.evaluateJavascript(js, null)
                        delay(500)
                    }
                }
                "check" -> {
                    val text = step.target?.get("text") as? String
                    if (text != null) {
                        val js = "OctoAI.checkCheckbox(OctoAI.findCheckboxByText('$text'))"
                        webView.evaluateJavascript(js, null)
                        delay(300)
                    }
                }
                "uncheck" -> {
                    val text = step.target?.get("text") as? String
                    if (text != null) {
                        val js = "OctoAI.uncheckCheckbox(OctoAI.findCheckboxByText('$text'))"
                        webView.evaluateJavascript(js, null)
                        delay(300)
                    }
                }
                "drag" -> {
                    val fromText = step.from?.get("text") as? String
                    val toText = step.to?.get("text") as? String
                    if (fromText != null && toText != null) {
                        val js = "OctoAI.dragDrop(OctoAI.findByText('$fromText'), OctoAI.findByText('$toText'))"
                        webView.evaluateJavascript(js, null)
                        delay(500)
                    }
                }
                "type" -> {
                    val text = step.text ?: ""
                    val targetId = step.target?.get("id") as? String
                    if (targetId != null) {
                        val js = "OctoAI.typeText(document.getElementById('$targetId'), '$text')"
                        webView.evaluateJavascript(js, null)
                        delay(300)
                    }
                }
                "clickNext" -> {
                    val jsNext = """
                        (function(){
                          var labels = ['далее','дальше','продолжить','next','continue'];
                          var cands = Array.from(document.querySelectorAll('button, a[role="button"], [role="button"], .MuiButtonBase-root, .MuiButton-root, .btn, .Button'));
                          for(var i=0;i<cands.length;i++){
                            var el=cands[i];
                            var t=OctoAI.text(el);
                            if(labels.includes(t) || labels.some(x=>t.startsWith(x))){
                              el.click();
                              return true;
                            }
                          }
                          return false;
                        })();
                    """.trimIndent()
                    webView.evaluateJavascript(jsNext, null)
                    delay(1000)
                }
            }
        }
    }

    fun parsePlan(json: String): ActionPlan? {
        return try {
            Gson().fromJson(json, ActionPlan::class.java)
        } catch (_: Throwable) {
            null
        }
    }
}
