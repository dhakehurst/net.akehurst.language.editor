package net.akehurst.language.editor.demo.gui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import net.akehurst.kotlin.compose.components.AlertDialogWithIssues
import net.akehurst.kotlin.compose.components.AlertInfo
import net.akehurst.kotlin.compose.layout.multipane.MultiPaneLayout
import net.akehurst.language.editor.browser.demo.application_editor_demo.generated.resources.Res
import net.akehurst.language.editor.information.Examples
import org.jetbrains.compose.resources.ExperimentalResourceApi

class GuiView {

    companion object {}

    private enum class ContentTab(val title: String) {
        Sentence("Sentence"),
        Language("Language"),
        Configuration("Configuration")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun content(stateHolder: GuiStateHolder) {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        var drawState by mutableStateOf(DrawerState(DrawerValue.Closed))
        var selectedTab by remember { mutableStateOf(ContentTab.Sentence) }

        MaterialTheme {
            ModalNavigationDrawer(
                gesturesEnabled = drawState.isOpen,
                drawerState = drawState,
                drawerContent = {
                    ModalDrawerSheet {
                        NavigationDrawerItem(
                            label = { Text(text = "Open") },
                            selected = false,
                            icon = {
                                Icon(
                                    imageVector = GuiTheme.Icons.FOLDER,
                                    contentDescription = "Open"
                                )
                            },
                            onClick = {
                                scope.launch {
                                    stateHolder.handler_openLanguageDirectory()
                                    drawState.close()
                                }
                            }
                        )
                        for (eg in Examples.map) {
                            NavigationDrawerItem(
                                label = { Text(text = eg.value.label) },
                                selected = false,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Example"
                                    )
                                },
                                onClick = {
                                    scope.launch {
                                        stateHolder.handler_loadExample(eg.value)
                                        drawState.close()
                                    }
                                }
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("AGL Editor") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawState.apply {
                                            if (isClosed) {
                                                open()
                                            } else {
                                                close()
                                            }
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Toggle drawer"
                                    )
                                }
                            },
                            actions = {
                                button(navController, GuiStateHolder.Companion.ViewMode.SENTENCE.name, GuiStateHolder.Companion.ViewMode.SENTENCE)
                                Spacer(modifier = Modifier.width(5.dp))
                                button(navController, GuiStateHolder.Companion.ViewMode.LANGUAGE.name, GuiStateHolder.Companion.ViewMode.LANGUAGE)
                                Spacer(modifier = Modifier.width(5.dp))
                                button(navController, GuiStateHolder.Companion.ViewMode.CONFIGURE.name, GuiStateHolder.Companion.ViewMode.CONFIGURE)
                            }
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            modifier = Modifier
                                .padding(0.dp)
                                .height(20.dp)
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(0.dp)
                            ) {

                            }
                        }
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = GuiStateHolder.Companion.ViewMode.SENTENCE.name
                        ) {
                            composable(GuiStateHolder.Companion.ViewMode.SENTENCE.name) { SentenceModeView(stateHolder.sentenceMode) }
                            composable(GuiStateHolder.Companion.ViewMode.LANGUAGE.name) { LanguageModeView(stateHolder.languageMode) }
                            composable(GuiStateHolder.Companion.ViewMode.CONFIGURE.name) { ConfigureModeView(stateHolder.configureMode) }
                        }
                    }

                    val modalDialog = stateHolder.modalDialog
                    if (null != modalDialog) {
                        when (modalDialog) {
                            is AlertInfo -> AlertDialogWithIssues(
                                modalDialog,
                                { stateHolder.modalDialog = null },
                                { stateHolder.modalDialog = null },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun button(nav: NavHostController, text: String, forMode: GuiStateHolder.Companion.ViewMode) {
        Button(
            onClick = { nav.navigate(forMode.name) },
            shape = MaterialTheme.shapes.extraSmall,
            contentPadding = PaddingValues(5.dp),
            border = if (nav.currentDestination?.route == forMode.name) BorderStroke(1.dp, Color.Black) else null,
            modifier = Modifier.padding(0.dp).height(30.dp)
        ) {
            Icons.Default.Edit
            Spacer(modifier = Modifier.width(5.dp))
            Text(text)
        }
    }
}

@Composable
fun SentenceModeView(state: SentenceModeStateHolder) {
    MultiPaneLayout(state.layout)
}

@Composable
fun LanguageModeView(state: LanguageModeStateHolder) {
    MultiPaneLayout(state.layout)
}

@Composable
fun ConfigureModeView(state: ConfigureModeStateHolder) {
}