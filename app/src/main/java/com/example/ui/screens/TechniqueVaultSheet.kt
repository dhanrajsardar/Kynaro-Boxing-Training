package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoxingCategory
import com.example.model.BoxingTechnique
import com.example.model.BoxingTechniqueRepository
import com.example.ui.components.CoachCharacter3DView
import com.example.ui.components.KynaroPrimaryButton
import com.example.ui.theme.KynaroBlack
import com.example.ui.theme.KynaroCardBg
import com.example.ui.theme.KynaroCardBorder
import com.example.ui.theme.KynaroGreen
import com.example.ui.theme.KynaroRed
import com.example.ui.theme.KynaroTextMuted
import com.example.ui.theme.KynaroTextSecondary
import com.example.ui.theme.KynaroTextWhite
import com.example.ui.theme.KynaroYellow
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniqueVaultSheet(
    onDismiss: () -> Unit,
    onLaunchTechniqueDrill: (BoxingTechnique) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<BoxingCategory?>(null) }
    var inspectingTechnique by remember { mutableStateOf<BoxingTechnique?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KynaroBlack,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        if (inspectingTechnique != null) {
            TechniqueDetailView(
                technique = inspectingTechnique!!,
                onBack = { inspectingTechnique = null },
                onLaunchDrill = { onLaunchTechniqueDrill(inspectingTechnique!!) }
            )
        } else {
            TechniqueGalleryView(
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                onInspectTechnique = { inspectingTechnique = it },
                onClose = onDismiss
            )
        }
    }
}

@Composable
private fun TechniqueGalleryView(
    selectedCategory: BoxingCategory?,
    onSelectCategory: (BoxingCategory?) -> Unit,
    onInspectTechnique: (BoxingTechnique) -> Unit,
    onClose: () -> Unit
) {
    val techniques = remember(selectedCategory) {
        if (selectedCategory == null) {
            BoxingTechniqueRepository.allTechniques
        } else {
            BoxingTechniqueRepository.allTechniques.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BIOMECHANICS VAULT",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KynaroRed,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "TECHNIQUE MASTERCLASS",
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = KynaroTextWhite
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KynaroCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KynaroTextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Study the canonical 1–6 punch system, defensive movement, and footwork with 360° 3D coach biomechanics.",
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                CategoryChip(
                    label = "ALL (12)",
                    isSelected = selectedCategory == null,
                    onClick = { onSelectCategory(null) }
                )
            }
            items(BoxingCategory.entries.toTypedArray()) { category ->
                CategoryChip(
                    label = category.label,
                    isSelected = selectedCategory == category,
                    onClick = { onSelectCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Technique Cards List
        techniques.forEach { technique ->
            TechniqueCard(
                technique = technique,
                onClick = { onInspectTechnique(technique) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) KynaroRed else KynaroCardBg)
            .border(
                1.dp,
                if (isSelected) KynaroRed else KynaroCardBorder,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) KynaroBlack else KynaroTextSecondary
        )
    }
}

@Composable
private fun TechniqueCard(
    technique: BoxingTechnique,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KynaroCardBg)
            .border(1.dp, KynaroCardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Punch Number Badge (e.g. "1", "2", "3")
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (technique.category == BoxingCategory.PUNCH)
                        Color(0xFF1A1608)
                    else
                        Color(0xFF0F1B2B)
                )
                .border(
                    1.dp,
                    if (technique.category == BoxingCategory.PUNCH)
                        KynaroRed.copy(alpha = 0.5f)
                    else
                        Color(0xFF3B82F6).copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = technique.canonicalNumber,
                fontFamily = OswaldFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (technique.category == BoxingCategory.PUNCH) KynaroRed else Color(0xFF60A5FA)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = technique.name,
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = KynaroTextWhite
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1F1F24))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = technique.level,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KynaroYellow
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = technique.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = KynaroTextSecondary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mastered by: ${technique.hallOfFameMaster}",
                style = MaterialTheme.typography.bodySmall,
                color = KynaroTextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Inspect",
            tint = KynaroRed,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TechniqueDetailView(
    technique: BoxingTechnique,
    onBack: () -> Unit,
    onLaunchDrill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Back Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KynaroCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = KynaroTextWhite
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1A1608))
                    .border(1.dp, KynaroRed.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CANONICAL PUNCH #${technique.canonicalNumber}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KynaroRed
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3D Coach Demonstration with 360° Touch Orbit!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF141107), KynaroCardBg)
                    )
                )
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            CoachCharacter3DView(
                pose = technique.pose,
                allowCameraOrbit = true,
                showAnatomicalHighlights = true,
                modifier = Modifier.fillMaxSize()
            )

            // 360 Orbit hint
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(CircleShape)
                    .background(KynaroBlack.copy(alpha = 0.75f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Drag to rotate 3D coach 360°",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = KynaroTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Technique Title & Master
        Text(
            text = technique.name.uppercase(),
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = KynaroTextWhite
        )
        Text(
            text = technique.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = KynaroTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Hall of Fame Legend Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E170A))
                .border(1.dp, KynaroYellow.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SportsMma,
                contentDescription = null,
                tint = KynaroYellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "HALL OF FAME BENCHMARK",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KynaroYellow
                )
                Text(
                    text = technique.hallOfFameMaster,
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = KynaroTextWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Kinetic Chain Summary
        Text(
            text = "KINETIC CHAIN FLOW",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = KynaroRed,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(KynaroCardBg)
                .border(1.dp, KynaroCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = technique.kineticChainSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = KynaroTextWhite,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4 Biomechanical Checkpoints
        Text(
            text = "BIOMECHANICAL CHECKPOINTS",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = KynaroTextWhite,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        technique.checkpoints.forEach { checkpoint ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = KynaroGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = checkpoint.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KynaroTextWhite
                    )
                    Text(
                        text = checkpoint.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = KynaroTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Amateur Flaw vs Pro Fix
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Flaw Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1608))
                    .border(1.dp, KynaroRed.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = KynaroRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AMATEUR FLAW",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KynaroRed
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = technique.amateurFlaw,
                    style = MaterialTheme.typography.bodySmall,
                    color = KynaroTextWhite,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            // Pro Fix Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF07241A))
                    .border(1.dp, KynaroGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = KynaroGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRO CORRECTION",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KynaroGreen
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = technique.proFix,
                    style = MaterialTheme.typography.bodySmall,
                    color = KynaroTextWhite,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action CTA
        KynaroPrimaryButton(
            text = "PRACTICE WITH 3D COACH",
            onClick = onLaunchDrill,
            testTag = "technique_practice_button"
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
