package me.bmax.apatch.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.UmountConfig
import me.bmax.apatch.ui.component.UmountConfigManager
import me.bmax.apatch.util.ui.showToast

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmountConfigScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(UmountConfigManager.isEnabled.value) }
    var paths by remember { mutableStateOf(UmountConfigManager.paths.value) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val config = UmountConfigManager.loadConfig(context)
        isEnabled = config.enabled
        paths = config.paths
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(stringResource(R.string.umount_config_title)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(android.R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 功能启用开关
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.umount_config_enabled),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.umount_config_enabled_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newEnabled ->
                            // 只更新状态，不立即执行
                            // 保存时统一处理服务开关
                            isEnabled = newEnabled
                        }
                    )
                }
            }

            // 路径输入框
            OutlinedTextField(
                value = paths,
                onValueChange = { paths = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text(stringResource(R.string.umount_config_paths_label)) },
                placeholder = { Text(stringResource(R.string.umount_config_paths_placeholder)) },
                minLines = 5,
                maxLines = Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                supportingText = {
                    Text(stringResource(R.string.umount_config_paths_helper))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.umount_config_save))
            }
        }
    }

    // 保存确认对话框
    if (showSaveDialog) {
        BasicAlertDialog(
            onDismissRequest = { showSaveDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                color = AlertDialogDefaults.containerColor,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.umount_config_save_confirm),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val config = UmountConfig(enabled = isEnabled, paths = paths)
                            val success = UmountConfigManager.saveConfig(context, config)
                            if (success) {
                                showToast(context, context.getString(R.string.umount_config_save_success))
                                navigator.navigateUp()
                            } else {
                                showToast(context, context.getString(R.string.umount_config_save_failed))
                            }
                            showSaveDialog = false
                        }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}
