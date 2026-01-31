package org.bxkr.octodiary.components.debugutils


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.Prefs
import org.bxkr.octodiary.clear
import org.bxkr.octodiary.raw
import org.bxkr.octodiary.save

@Composable
fun MainActivity.PrefEditor(clear: () -> Unit) {
    Dialog(
        onDismissRequest = { clear() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface {
                    Text(
                        "Preference editor",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Surface(onClick = { clear() }) {
                    Icon(Icons.Default.Close, "close", Modifier.padding(16.dp))
                }
            }
            var textFieldValue by remember { mutableStateOf("") }
            var confirmedKey by remember { mutableStateOf<String?>(null) }
            TextField(textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    confirmedKey = null
                },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { confirmedKey = textFieldValue }) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, "go")
                    }
                },
                label = { Text(text = "Storage name") })
            AnimatedVisibility(confirmedKey != null) {
                Surface { confirmedKey?.let { Editor(it) } }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Context.Editor(key: String) {
    val prefs = object : Prefs(key, this) {}
    val all = prefs.raw.all
    var currentFunction: @Composable () -> Unit by remember { mutableStateOf({}) }
    LazyColumn(Modifier.padding(16.dp)) {
        item {
            Button(onClick = {
                currentFunction = {
                    AddPref({ name, value -> prefs.save(name to value) }) {
                        currentFunction = {}
                    }
                }
            }, Modifier.fillMaxWidth()) {
                Text(text = "Add")
            }
            AnimatedVisibility(all.isNotEmpty()) {
                var confirmNeeded by remember { mutableStateOf(false) }
                OutlinedButton({
                    if (confirmNeeded) {
                        prefs.clear()
                        currentFunction = {}
                        confirmNeeded = false
                    } else confirmNeeded = true
                }, Modifier.fillMaxWidth()) {
                    AnimatedVisibility(!confirmNeeded) {
                        Text("Delete this storage")
                    }
                    AnimatedVisibility(confirmNeeded) {
                        Text("Confirm")
                    }
                }
            }
        }
        items(all.toList()) {
            Column {
                var showMenu by remember { mutableStateOf(false) }
                Row(
                    Modifier
                        .combinedClickable(onLongClick = {
                            showMenu = true
                        }) {
                            currentFunction = {
                                EditPref(it.second, { it1 ->
                                    prefs.save(it.first to it1)
                                }) { currentFunction = {} }
                            }
                        }
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        it.first,
                        Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        it.second.toString(), maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }
                val clipboardManager = LocalClipboardManager.current
                val copy = { it: String -> clipboardManager.setText(AnnotatedString(it)) }
                DropdownMenu(showMenu, { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Copy key") }, onClick = {
                        copy(it.first)
                        showMenu = false
                    })
                    DropdownMenuItem(text = { Text("Copy value") }, onClick = {
                        copy(it.second.toString())
                        showMenu = false
                    })
                    DropdownMenuItem(text = { Text("Copy all storage in JSON") }, onClick = {
                        copy(Gson().toJson(all))
                        showMenu = false
                    })
                }
                HorizontalDivider()
            }
        }
    }
    currentFunction()
}

@Composable
private fun EditPref(pref: Any?, setFn: (Any?) -> Unit, clear: () -> Unit) {
    var prefState: Any? by remember { mutableStateOf(pref) }
    var isIllegal: Boolean by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = { clear() }, confirmButton = {
        TextButton(onClick = {
            clear()
            setFn(prefState)
        }, enabled = !isIllegal) {
            Text("Save")
        }
    }, dismissButton = {
        TextButton(onClick = {
            clear()
            setFn(null)
        }) {
            Text("Delete")
        }
    }, text = {
        Column {
            Text(
                "Type: ${
                    if (pref != null) {
                        pref::class.simpleName
                    } else "null"
                }"
            )
            if (pref != null) {
                var inputValue by remember { mutableStateOf(prefState.toString()) }
                if (inputValue.length < 10000) {
                    TextField(
                        value = inputValue,
                        isError = isIllegal,
                        supportingText = {
                            AnimatedContent(isIllegal) { boolean ->
                                Text(if (boolean) "Illegal input string" else "")
                            }
                        },
                        onValueChange = {
                            inputValue = it
                            try {
                                prefState = when (pref) {
                                    is String -> it
                                    is Boolean -> it.toBooleanStrict()
                                    is Int -> it.toInt()
                                    is Long -> it.toLong()
                                    is Float -> it.toFloat()
                                    else -> prefState
                                }
                                isIllegal = false
                            } catch (e: IllegalArgumentException) {
                                isIllegal = true
                            }
                        },
                        singleLine = true
                    )
                } else {
                    Text("Too long string!")
                }
            }
        }
    })
}

@Composable
private fun AddPref(saveFn: ((String, Any) -> Unit), clear: () -> Unit) {
    var saveFunction by remember { mutableStateOf({}) }
    AlertDialog(onDismissRequest = { clear() }, confirmButton = {
        Button(onClick = {
            saveFunction()
            clear()
        }) { Text("Save") }
    }, text = {
        Column {
            var inputKeyValue: String by remember { mutableStateOf("") }
            TextField(
                value = inputKeyValue,
                onValueChange = { inputKeyValue = it },
                singleLine = true,
                label = { Text("Key") },
                supportingText = { Text("") }
            )

            val allowedTypeNames = remember {
                PrefTypes.values().map { it.sample::class.simpleName to it.sample }.toMap()
            }

            var inputValue: String by remember { mutableStateOf("0") }
            var selectedValue: Any by remember { mutableStateOf(0) }
            var isIllegal by remember { mutableStateOf(false) }

            var inputValueType by remember { mutableStateOf("Int") }
            var selectedType by remember { mutableStateOf(inputValueType) }
            var illegalType by remember { mutableStateOf(false) }
            TextField(value = inputValueType, onValueChange = {
                inputValueType = it
                illegalType = inputValueType !in allowedTypeNames.keys
                if (!illegalType) {
                    selectedType = it
                    val enumObject =
                        PrefTypes.values().first { it.sample::class.simpleName == selectedType }
                    inputValue = enumObject.sample.toString()
                    selectedValue = enumObject.sample
                } else inputValue = ""
            }, isError = illegalType, supportingText = {
                AnimatedContent(illegalType) { boolean ->
                    Text(if (boolean) "Illegal type name" else "")
                }
            }, label = { Text("Type") }, singleLine = true)

            TextField(value = inputValue, onValueChange = {
                inputValue = it
                try {
                    val enumObject =
                        PrefTypes.values().first { it.sample::class.simpleName == selectedType }
                    selectedValue = enumObject.castFn(inputValue)
                    isIllegal = false
                } catch (e: IllegalArgumentException) {
                    isIllegal = true
                }
            }, enabled = !illegalType, isError = isIllegal, supportingText = {
                AnimatedContent(isIllegal) { boolean ->
                    Text(if (boolean) "Illegal input string" else "")
                }
            }, label = { Text("Value") }, singleLine = true)

            saveFunction = {
                saveFn(inputKeyValue, selectedValue)
            }
        }
    })
}

private enum class PrefTypes(val sample: Any, val castFn: (String) -> Any) {
    StringT("", { it }),
    BooleanT(true, { it.toBooleanStrict() }),
    IntT(0, { it.toInt() }),
    LongT(0L, { it.toLong() }),
    FloatT(0f, { it.toFloat() })
}


