package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.ui.components.DisclosureSection
import com.lkovari.mobile.apps.treecalc.ui.components.TreeCalcBrandHeader
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val email = stringResource(R.string.about_email)
    val sourceUrl = stringResource(R.string.about_source_url)
    val privacyUrl = stringResource(R.string.about_privacy_url)
    val palette = LocalTreeCalcPalette.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
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
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TreeCalcBrandHeader()
            Spacer(modifier = Modifier.height(20.dp))
            var open by rememberSaveable { mutableIntStateOf(0) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DisclosureSection(
                    title = stringResource(R.string.about_function_title),
                    expanded = open == 0,
                    onToggle = { open = if (open == 0) -1 else 0 }
                ) {
                    BodyText(stringResource(R.string.about_functionality))
                }
                DisclosureSection(
                    title = stringResource(R.string.about_goal_title),
                    expanded = open == 1,
                    onToggle = { open = if (open == 1) -1 else 1 }
                ) {
                    BodyText(stringResource(R.string.about_goal))
                }
                DisclosureSection(
                    title = stringResource(R.string.about_source_title),
                    expanded = open == 2,
                    onToggle = { open = if (open == 2) -1 else 2 }
                ) {
                    BodyText(stringResource(R.string.about_source))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sourceUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.link,
                        modifier = Modifier.clickable { uriHandler.openUri(sourceUrl) }
                    )
                }
                DisclosureSection(
                    title = stringResource(R.string.about_privacy),
                    expanded = open == 3,
                    onToggle = { open = if (open == 3) -1 else 3 }
                ) {
                    BodyText(stringResource(R.string.about_privacy_body))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = privacyUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.link,
                        modifier = Modifier.clickable { uriHandler.openUri(privacyUrl) }
                    )
                }
                DisclosureSection(
                    title = stringResource(R.string.about_support_title),
                    expanded = open == 4,
                    onToggle = { open = if (open == 4) -1 else 4 }
                ) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.link,
                        modifier = Modifier.clickable { uriHandler.openUri("mailto:$email") }
                    )
                }
                DisclosureSection(
                    title = stringResource(R.string.about_copyright_title),
                    expanded = open == 5,
                    onToggle = { open = if (open == 5) -1 else 5 }
                ) {
                    BodyText(stringResource(R.string.about_author))
                }
            }
        }
        }
    }
}
