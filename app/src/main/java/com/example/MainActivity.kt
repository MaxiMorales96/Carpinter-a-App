package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.model.CarpentryBudget
import com.example.ui.components.FabricaLogo
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BudgetViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    CarpentryApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CarpentryApp(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // UI states
    val budgets by viewModel.budgetsState.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val carpenterName by viewModel.carpenterName.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val logoPath by viewModel.logoPath.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Crear Presupuesto, 1 = Historial
    var showProfileEditor by remember { mutableStateOf(false) }

    // Profiles Form state
    var editBusName by remember(businessName) { mutableStateOf(businessName) }
    var editCarpName by remember(carpenterName) { mutableStateOf(carpenterName) }
    var editPhone by remember(businessPhone) { mutableStateOf(businessPhone) }

    // Active Budget Form State
    var clientName by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var detailsDescription by remember { mutableStateOf("") }
    var materialsCostStr by remember { mutableStateOf("") }
    var laborCostStr by remember { mutableStateOf("") }
    var otherCostStr by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("") }

    // Selected budget for detail dialog view
    var selectedBudgetDetail by remember { mutableStateOf<CarpentryBudget?>(null) }

    // Image Picker Launcher
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveLogoUri(it)
            Toast.makeText(context, "¡Logotipo guardado con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Elegant Wood Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, CircleShape)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FabricaLogo(
                            modifier = Modifier.fillMaxSize(),
                            showText = false
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = businessName.ifBlank { "Fábrica de Muebles a Medida" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (carpenterName.isNotBlank()) "Carpintero: $carpenterName" else "Presupuestos Profesionales",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Edit Profile Icon
                IconButton(
                    onClick = { showProfileEditor = !showProfileEditor },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (showProfileEditor) Icons.Filled.Close else Icons.Filled.Settings,
                        contentDescription = "Configurar Perfil de Carpintería",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Profile Quick Editor Drawer Panel
        AnimatedVisibility(
            visible = showProfileEditor,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Configurar Perfil de Carpintería",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Business Logo preview frame
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .testTag("logo_picker")
                                .clickable { logoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoPath != null) {
                                AsyncImage(
                                    model = logoPath,
                                    contentDescription = "Logotipo personalizado de Carpintería",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FabricaLogo(
                                        modifier = Modifier.fillMaxSize(),
                                        showText = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Logotipo de Marca",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Sube una foto de tu logo. Se imprimirá en el cabezal de todos tus presupuestos para verse más profesional.",
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            if (logoPath != null) {
                                Text(
                                    "Quitar logo",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { 
                                            viewModel.removeLogo()
                                            Toast.makeText(context, "Logotipo quitado", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editBusName,
                        onValueChange = { editBusName = it },
                        label = { Text("Nombre de la Carpintería o Negocio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCarpName,
                            onValueChange = { editCarpName = it },
                            label = { Text("Tu Nombre") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Teléfono de Contacto") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(editBusName, editCarpName, editPhone)
                            showProfileEditor = false
                            Toast.makeText(context, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios de Perfil")
                    }
                }
            }
        }

        // Two Main Tabs Navigation Row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Nuevo Presupuesto", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                icon = { Icon(Icons.Filled.PostAdd, contentDescription = "Formulario") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Historial ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(budgets.size.toString())
                        }
                    }
                },
                icon = { Icon(Icons.Filled.History, contentDescription = "Historial") }
            )
        }

        // Contents Based on Tab Selected
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (activeTab == 0) {
                // TAB 0: CREATE NEW BUDGET
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 42.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Person,
                                        contentDescription = "Cliente",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Datos del Cliente y Proyecto",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text("Nombre del Cliente") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("client_name_input")
                                        .padding(vertical = 4.dp),
                                    placeholder = { Text("Ej. Juan Gómez") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = projectName,
                                    onValueChange = { projectName = it },
                                    label = { Text("Nombre / Tipo del Proyecto") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("project_name_input")
                                        .padding(vertical = 4.dp),
                                    placeholder = { Text("Ej. Bajo Mesada en Melamina Oak") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // DETALLES TEXT SECTION (Highly requested!)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.EditNote,
                                        contentDescription = "Detalles",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Especificación y Detalles del Trabajo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "Escribe libremente las medidas, maderas a usar (roble, pino, MDF), colores, herrajes y cualquier detalle constructivo relevante.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = detailsDescription,
                                    onValueChange = { detailsDescription = it },
                                    label = { Text("Detalles y Notas del Mueble") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 110.dp)
                                        .testTag("details_input"),
                                    placeholder = { Text("Ej: Mesa de Roble macizo de 2.20 x 0.90 metros con patas de hierro perfil estructural en 'X'. Acabado laca poliuretánica satinada. Bisagras de cazoleta de 35mm con freno...") },
                                    maxLines = 10,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // COSTS SECTION (Taxes removed!)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Payments,
                                        contentDescription = "Costos",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Desglose de Costos ($ ARS / locales)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = materialsCostStr,
                                        onValueChange = { materialsCostStr = it },
                                        label = { Text("Materiales ($)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = laborCostStr,
                                        onValueChange = { laborCostStr = it },
                                        label = { Text("Mano de Obra ($)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = otherCostStr,
                                        onValueChange = { otherCostStr = it },
                                        label = { Text("Flete u Otros ($)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = discountStr,
                                        onValueChange = { discountStr = it },
                                        label = { Text("Descuento ($)") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                val matValue = materialsCostStr.toDoubleOrNull() ?: 0.0
                                val labValue = laborCostStr.toDoubleOrNull() ?: 0.0
                                val othValue = otherCostStr.toDoubleOrNull() ?: 0.0
                                val disValue = discountStr.toDoubleOrNull() ?: 0.0
                                val subtotalNetValue = matValue + labValue + othValue
                                val totalNetValue = (subtotalNetValue - disValue).coerceAtLeast(0.0)

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Total Neto del Presupuesto",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "$ ${String.format("%,.2f", totalNetValue)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // PAPER SHEET PREVIEW (LIVE RENDER)
                    item {
                        val matValue = materialsCostStr.toDoubleOrNull() ?: 0.0
                        val labValue = laborCostStr.toDoubleOrNull() ?: 0.0
                        val othValue = otherCostStr.toDoubleOrNull() ?: 0.0
                        val disValue = discountStr.toDoubleOrNull() ?: 0.0
                        val totalNetValue = (matValue + labValue + othValue - disValue).coerceAtLeast(0.0)

                        Text(
                            text = "VISTA PREVIA DEL DOCUMENTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )

                        // Invoice Sheet Card
                        InvoiceSheet(
                            businessName = businessName,
                            logoPath = logoPath,
                            phone = businessPhone,
                            carpenterName = carpenterName,
                            clientName = clientName,
                            projectName = projectName,
                            description = detailsDescription,
                            materialsCost = matValue,
                            laborCost = labValue,
                            otherCosts = othValue,
                            discount = disValue,
                            totalNet = totalNetValue
                        )
                    }

                    // SAVE / SHARE BUTTONS ROW
                    item {
                        val matValue = materialsCostStr.toDoubleOrNull() ?: 0.0
                        val labValue = laborCostStr.toDoubleOrNull() ?: 0.0
                        val othValue = otherCostStr.toDoubleOrNull() ?: 0.0
                        val disValue = discountStr.toDoubleOrNull() ?: 0.0
                        val totalNetValue = (matValue + labValue + othValue - disValue).coerceAtLeast(0.0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (clientName.isBlank() || projectName.isBlank()) {
                                        Toast.makeText(context, "Por favor completa Cliente y Proyecto primero", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    viewModel.createBudget(
                                        clientName = clientName,
                                        projectName = projectName,
                                        description = detailsDescription,
                                        materials = matValue,
                                        labor = labValue,
                                        other = othValue,
                                        discount = disValue
                                    ) {
                                        // Clear fields
                                        clientName = ""
                                        projectName = ""
                                        detailsDescription = ""
                                        materialsCostStr = ""
                                        laborCostStr = ""
                                        otherCostStr = ""
                                        discountStr = ""
                                        Toast.makeText(context, "¡Presupuesto Guardado con Éxito!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("submit_budget_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.CloudUpload, contentDescription = "Guardar Budget", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar en base", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (clientName.isBlank() || projectName.isBlank()) {
                                        Toast.makeText(context, "Completa el presupuesto para poder compartirlo", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }
                                    shareBudgetText(
                                        context = context,
                                        businessName = businessName,
                                        businessPhone = businessPhone,
                                        clientName = clientName,
                                        projectName = projectName,
                                        description = detailsDescription,
                                        materialsCost = matValue,
                                        laborCost = labValue,
                                        otherCosts = othValue,
                                        discount = disValue,
                                        totalNet = totalNetValue
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Compartir", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enviar por WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // TAB 1: HISTORY OF SAVED BUDGETS
                if (budgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = "Ningún presupuesto guardado",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Aún no tienes presupuestos guardados",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ve a la pestaña 'Nuevo Presupuesto' para diseñar y registrar tu primer cotización de carpintería.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 42.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(budgets) { budget ->
                            val totalBudgetAmt = (budget.materialsCost + budget.laborCost + budget.otherCosts - budget.discount).coerceAtLeast(0.0)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBudgetDetail = budget },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = budget.projectName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Cliente: ${budget.clientName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(budget.date)),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            if (budget.description.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Filled.Description,
                                                        contentDescription = "Tiene detalles",
                                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        "Detalles",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$ ${String.format("%,.2f", totalBudgetAmt)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Total Neto",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    shareBudgetText(
                                                        context = context,
                                                        businessName = businessName,
                                                        businessPhone = businessPhone,
                                                        clientName = budget.clientName,
                                                        projectName = budget.projectName,
                                                        description = budget.description,
                                                        materialsCost = budget.materialsCost,
                                                        laborCost = budget.laborCost,
                                                        otherCosts = budget.otherCosts,
                                                        discount = budget.discount,
                                                        totalNet = totalBudgetAmt
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Share,
                                                    contentDescription = "Enviar presupuesto por mensaje",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteBudget(budget) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.DeleteOutline,
                                                    contentDescription = "Eliminar presupuesto guardado",
                                                    tint = Color.Red.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DETAIL AND RECEPT POP-UP DIALOG (Viewing historic item in detailed invoice format)
    selectedBudgetDetail?.let { budget ->
        val totalBudgetAmt = (budget.materialsCost + budget.laborCost + budget.otherCosts - budget.discount).coerceAtLeast(0.0)
        
        AlertDialog(
            onDismissRequest = { selectedBudgetDetail = null },
            confirmButton = {
                Button(
                    onClick = {
                        shareBudgetText(
                            context = context,
                            businessName = businessName,
                            businessPhone = businessPhone,
                            clientName = budget.clientName,
                            projectName = budget.projectName,
                            description = budget.description,
                            materialsCost = budget.materialsCost,
                            laborCost = budget.laborCost,
                            otherCosts = budget.otherCosts,
                            discount = budget.discount,
                            totalNet = totalBudgetAmt
                        )
                    },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar WhatsApp")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedBudgetDetail = null },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cerrar")
                }
            },
            title = {
                Text(
                    text = "Presupuesto de ${budget.projectName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            InvoiceSheet(
                                businessName = businessName,
                                logoPath = logoPath,
                                phone = businessPhone,
                                carpenterName = carpenterName,
                                clientName = budget.clientName,
                                projectName = budget.projectName,
                                description = budget.description,
                                materialsCost = budget.materialsCost,
                                laborCost = budget.laborCost,
                                otherCosts = budget.otherCosts,
                                discount = budget.discount,
                                totalNet = totalBudgetAmt
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * A beautiful sheet layout mimicking a paper printed carpentry quotation receipt.
 */
@Composable
fun InvoiceSheet(
    businessName: String,
    logoPath: String?,
    phone: String,
    carpenterName: String,
    clientName: String,
    projectName: String,
    description: String,
    materialsCost: Double,
    laborCost: Double,
    otherCosts: Double,
    discount: Double,
    totalNet: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(1.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Document Header: Logo & Brand Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (logoPath != null) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = logoPath,
                            contentDescription = "Mi Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 82.dp, height = 56.dp)
                            .background(Color.White)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FabricaLogo(
                            modifier = Modifier.fillMaxSize(),
                            showText = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = businessName.ifBlank { "Fábrica de Muebles a Medida" }.uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (phone.isNotBlank()) {
                        Text(
                            text = "Tel: $phone",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (carpenterName.isNotBlank()) {
                        Text(
                            text = "Por: $carpenterName",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "PRESUPUESTO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                thickness = 1.5.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Client and Project Lines
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text(
                        "CLIENTE: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        text = clientName.ifBlank { "—" },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text(
                        "PROYECTO: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(70.dp)
                    )
                    Text(
                        text = projectName.ifBlank { "—" },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // DETALLES BLOCK (Highly styled for custom notes entry!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "DETALLES Y ESPECIFICACIÓN TÉCNICA:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = description.ifBlank { "No se especificaron detalles adicionales para este mueble." },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        fontStyle = if (description.isBlank()) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Costs Ledger Table Mockup
            Text(
                "DESGLOSE DE VALORES",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RowValueLine("Materiales de Carpintería", materialsCost)
                RowValueLine("Mano de Obra / Confección", laborCost)
                if (otherCosts > 0) {
                    RowValueLine("Gastos Adicionales (Flete/Herrajes)", otherCosts)
                }
                if (discount > 0) {
                    RowValueLine("Descuento Promocional", -discount, isDiscount = true)
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL NETO",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$ ${String.format("%,.2f", totalNet)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Presupuesto válido por 15 días. Sujeto a cambios de valores de materiales.",
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RowValueLine(label: String, value: Double, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isDiscount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = if (isDiscount) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = if (isDiscount) "- $ ${String.format("%,.2f", -value)}" else "$ ${String.format("%,.2f", value)}",
            fontSize = 11.sp,
            color = if (isDiscount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Standard Android Text Sharer to forward beautiful text catalogs to clients via messaging apps
 */
fun shareBudgetText(
    context: Context,
    businessName: String,
    businessPhone: String,
    clientName: String,
    projectName: String,
    description: String,
    materialsCost: Double,
    laborCost: Double,
    otherCosts: Double,
    discount: Double,
    totalNet: Double
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val finalBusinessName = businessName.ifBlank { "Fábrica de Muebles a Medida" }
    
    val textToSend = buildString {
        appendLine("📐 *PRESUPUESTO DE CARPINTERÍA* 📐")
        appendLine("==================================")
        appendLine("*De:* $finalBusinessName")
        if (businessPhone.isNotBlank()) {
            appendLine("*Contacto:* $businessPhone")
        }
        appendLine("==================================")
        appendLine("*Fecha:* $dateStr")
        appendLine("*Cliente:* ${clientName.ifBlank { "Estimado Cliente" }}")
        appendLine("*Proyecto:* ${projectName.ifBlank { "Trabajo a Medida" }}")
        appendLine()
        
        if (description.isNotBlank()) {
            appendLine("📝 *Detalles y Especificaciones:*")
            appendLine(description)
            appendLine()
        }
        
        appendLine("💰 *Desglose de Costos:*")
        appendLine("  • Materiales: $ ${String.format("%,.2f", materialsCost)}")
        appendLine("  • Mano de Obra: $ ${String.format("%,.2f", laborCost)}")
        if (otherCosts > 0) {
            appendLine("  • Flete / Instalación / Herrajes: $ ${String.format("%,.2f", otherCosts)}")
        }
        if (discount > 0) {
            appendLine("  • Descuento: - $ ${String.format("%,.2f", discount)}")
        }
        appendLine("----------------------------------")
        appendLine("*TOTAL NETO:* $ ${String.format("%,.2f", totalNet)}")
        appendLine("==================================")
        appendLine("_Presupuesto válido por 15 días._")
        appendLine("_¡Muchas gracias por confiar en nuestro taller!_")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Presupuesto de Carpintería - $projectName")
        putExtra(Intent.EXTRA_TEXT, textToSend)
    }
    
    try {
        context.startActivity(Intent.createChooser(intent, "Enviar presupuesto vía..."))
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo invocar la acción de envío", Toast.LENGTH_SHORT).show()
    }
}
