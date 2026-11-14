package com.savii.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.savii.focus.model.FocusTask
import com.savii.focus.ui.FocusViewModel
import com.savii.focus.ui.theme.SaviiTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private enum class Tab(val label: String, val symbol: String) {
    Home("Home", "⌂"), Learn("Learn", "▤"), Planner("Planner", "✓"),
    Focus("Focus", "◷"), Insights("Insights", "▥")
}

class MainActivity : ComponentActivity() {
    private val viewModel: FocusViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaviiTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                SaviiApp(state, viewModel)
            }
        }
    }
}

@Composable
private fun SaviiApp(state: com.savii.focus.ui.AppState, vm: FocusViewModel) {
    var selected by remember { mutableStateOf(Tab.Home) }
    var showSettings by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Text(tab.symbol, fontSize = 20.sp) },
                        label = { Text(tab.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("SAVII", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("FOCUS", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                IconButton(onClick = { showSettings = true }) { Text("⚙", fontSize = 23.sp) }
            }
            when (selected) {
                Tab.Home -> HomeScreen(state, onTab = { selected = it })
                Tab.Learn -> LearnScreen(state, vm)
                Tab.Planner -> PlannerScreen(state.tasks, vm)
                Tab.Focus -> FocusScreen(state.subjects.map { it.name }.ifEmpty { listOf("General") }, vm)
                Tab.Insights -> InsightsScreen(state)
            }
        }
    }
    if (showSettings) SettingsDialog(onDismiss = { showSettings = false })
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp), content = content)
}

@Composable
private fun HomeScreen(state: com.savii.focus.ui.AppState, onTab: (Tab) -> Unit) {
    val completed = state.tasks.count { it.done }
    val minutes = state.sessions.sumOf { it.minutes }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Your life. In focus.", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("A calmer way to learn, plan and make progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Study time", "${minutes / 60}h ${minutes % 60}m", Modifier.weight(1f))
                StatCard("Tasks done", "$completed/${state.tasks.size}", Modifier.weight(1f))
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF241A3B)), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("READY WHEN YOU ARE", color = Color(0xFFCBB8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Make this moment count.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Start a focused session and give one task your attention.", color = Color(0xFFE0D8EF))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onTab(Tab.Focus) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9A72F5))) {
                        Text("Start focusing")
                    }
                }
            }
        }
        item { SectionTitle("Your workspace") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("Learning", "Subjects & notes", "▤", Modifier.weight(1f)) { onTab(Tab.Learn) }
                ActionCard("Planner", "Tasks & deadlines", "✓", Modifier.weight(1f)) { onTab(Tab.Planner) }
            }
        }
        item { SectionTitle("Next tasks") }
        if (state.tasks.isEmpty()) {
            item { EmptyCard("Nothing on your list yet", "Add a task in Planner to get started.") }
        } else {
            items(state.tasks.take(3), key = { it.id }) { task ->
                TaskRow(task, onToggle = {}, onDelete = {})
            }
        }
    }
}

@Composable
private fun LearnScreen(state: com.savii.focus.ui.AppState, vm: FocusViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Learning space", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Keep your subjects in one place.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showAdd = true }) { Text("+ Add") }
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(state.subjects, key = { it.id }) { subject ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(17.dp)) {
                        Text(subject.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(subject.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Text("Use Focus to study this subject", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }
            if (state.subjects.isEmpty()) item { EmptyCard("No subjects yet", "Add a subject to start organizing your learning.") }
        }
    }
    if (showAdd) AddSubjectDialog({ showAdd = false }) { name, detail -> vm.addSubject(name, detail); showAdd = false }
}

@Composable
private fun PlannerScreen(tasks: List<FocusTask>, vm: FocusViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Planner", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Small steps become big progress.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showAdd = true }) { Text("+ Task") }
        }
        Spacer(Modifier.height(12.dp))
        val open = tasks.count { !it.done }
        StatCard("Remaining tasks", open.toString(), Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(tasks, key = { it.id }) { task ->
                TaskRow(task, onToggle = { vm.toggleTask(task) }, onDelete = { vm.deleteTask(task) })
            }
            if (tasks.isEmpty()) item { EmptyCard("Your planner is clear", "Add your first task using the button above.") }
        }
    }
    if (showAdd) AddTaskDialog({ showAdd = false }) { title, due -> vm.addTask(title, due); showAdd = false }
}

@Composable
private fun FocusScreen(subjects: List<String>, vm: FocusViewModel) {
    var selectedSubject by remember(subjects) { mutableStateOf(subjects.first()) }
    var duration by remember { mutableIntStateOf(25) }
    var remaining by remember { mutableIntStateOf(duration * 60) }
    var running by remember { mutableStateOf(false) }
    var sessionSaved by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        if (running && remaining == 0 && !sessionSaved) {
            vm.addSession(selectedSubject, duration, true)
            sessionSaved = true
            running = false
        }
    }
    Page {
        Text("Focus mode", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("One thing at a time. You've got this.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF241A3B))
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DEEP WORK SESSION", color = Color(0xFFCBB8FF), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Text(String.format("%02d:%02d", remaining / 60, remaining % 60), color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light)
                Text(if (running) "Stay with the task" else "Ready to begin", color = Color(0xFFD8CDEB))
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        if (running) running = false else { if (remaining <= 0) remaining = duration * 60; sessionSaved = false; running = true }
                    }) { Text(if (running) "Pause" else "Start") }
                    OutlinedButton(onClick = { running = false; remaining = duration * 60; sessionSaved = false }) { Text("Reset") }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Choose a subject", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        var menu by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedSubject); Spacer(Modifier.weight(1f)); Text("⌄") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                subjects.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { selectedSubject = s; menu = false }) }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Session length", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 60).forEach { mins ->
                FilterChip(selected = duration == mins, onClick = { if (!running) { duration = mins; remaining = mins * 60; sessionSaved = false } }, label = { Text("$mins min") })
            }
        }
        if (sessionSaved) Text("Session saved to your progress.", color = MaterialTheme.colorScheme.primary)
        Text("Timer runs while this screen is active. Background timer and notifications can be added in a later release.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun InsightsScreen(state: com.savii.focus.ui.AppState) {
    val total = state.sessions.sumOf { it.minutes }
    val finished = state.sessions.count { it.completed }
    val tasksDone = state.tasks.count { it.done }
    Page {
        Text("Your progress", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("A simple view of the work you've put in.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        StatCard("Total focused time", "${total / 60}h ${total % 60}m", Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Sessions", finished.toString(), Modifier.weight(1f))
            StatCard("Tasks done", tasksDone.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("Recent sessions")
        if (state.sessions.isEmpty()) EmptyCard("Your story starts here", "Complete a focus session to see your progress.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.sessions.take(12), key = { it.id }) { session ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(session.subject, fontWeight = FontWeight.SemiBold)
                            Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(session.created)), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text("${session.minutes} min", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: FocusTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                if (task.due.isNotBlank()) Text(task.due, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(symbol, color = MaterialTheme.colorScheme.primary, fontSize = 24.sp)
            Spacer(Modifier.height(9.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
}

@Composable
private fun EmptyCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Task name") }, singleLine = true)
                OutlinedTextField(due, { due = it }, label = { Text("Due date (optional)") }, placeholder = { Text("e.g. 18 Nov") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(title.trim(), due.trim()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddSubjectDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Subject name") }, singleLine = true)
                OutlinedTextField(detail, { detail = it }, label = { Text("Short description") })
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), detail.trim()) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SAVII FOCUS  •  Version 1.0.0", fontWeight = FontWeight.SemiBold)
                Text("Your learning and productivity data is stored locally on this device.")
                Text("Cloud account and cross-device sync are not enabled in this offline-first build.")
                Text("AI Assistant connection is not configured. No fake AI responses are shown.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
