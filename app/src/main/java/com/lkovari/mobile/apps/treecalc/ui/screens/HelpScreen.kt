package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.BuildConfig
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.ui.components.DisclosureSection
import com.lkovari.mobile.apps.treecalc.ui.components.TreeCalcBrandHeader
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val palette = LocalTreeCalcPalette.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.screenWashTop,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = palette.screenWashTop
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(pastelScreenBrush(palette)),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TreeCalcBrandHeader()
            Spacer(modifier = Modifier.height(24.dp))
            val sections = listOf(
                R.string.help_swipe_title to R.string.help_swipe_body,
                R.string.help_bases_title to R.string.help_bases_body,
                R.string.help_convert_title to R.string.help_convert_body,
                R.string.help_equals_title to R.string.help_equals_body,
                R.string.help_angle_title to R.string.help_angle_body,
                R.string.help_repeating_title to R.string.help_repeating_body,
                R.string.help_playback_title to R.string.help_playback_body
            )
            var open by rememberSaveable { mutableIntStateOf(0) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sections.forEachIndexed { index, section ->
                    DisclosureSection(
                        title = stringResource(section.first),
                        expanded = open == index,
                        onToggle = { open = if (open == index) -1 else index }
                    ) {
                        Text(
                            text = stringResource(section.second),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.help_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.help_copyright),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }
}
