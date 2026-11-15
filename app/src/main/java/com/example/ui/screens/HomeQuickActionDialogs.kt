package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.data.model.PurchaseItem
import com.example.data.model.Supplier
import com.example.domain.BakiOverview
import com.example.ui.components.AccountPickerField
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.SearchGlassField
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import com.example.util.SyncStatus

@Composable
fun HomeNotificationsDialog(
    lowStockProducts: List<Product>,
    bakiOverview: BakiOverview,
    syncStatus: SyncStatus,
    currentLanguage: AppLanguage,
    canViewStock: Boolean,
    canAccessBaki: Boolean,
    onRestockProduct: (Product) -> Unit,
    onOpenLowStock: () -> Unit,
    onOpenBaki: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_notifications_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = DarkBackground,
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF192219), Color(0xFF101210))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ShopLocale.get("store_alerts_title", currentLanguage),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real Cloud Sync status banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x16FFFFFF),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (syncIcon, syncColor, syncLabel) = when (syncStatus) {
                            SyncStatus.ONLINE -> Triple(
                                Icons.Default.CloudDone,
                                VividGreen,
                                ShopLocale.get("sync_status_online", currentLanguage)
                            )
                            SyncStatus.SYNCING -> Triple(
                                Icons.Default.Sync,
                                Color(0xFFFFB300),
                                ShopLocale.get("syncing_changes", currentLanguage)
                            )
                            SyncStatus.OFFLINE -> Triple(
                                Icons.Default.CloudOff,
                                AlertRed,
                                ShopLocale.get("offline_cache_active", currentLanguage)
                            )
                        }
                        Icon(syncIcon, contentDescription = null, tint = syncColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncLabel,
                            color = syncColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (canViewStock) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ShopLocale.format("low_stock_count_header", currentLanguage, lowStockProducts.size),
                                    color = Color(0xFFFFB300),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (lowStockProducts.isNotEmpty()) {
                                    Text(
                                        text = ShopLocale.get("view_all", currentLanguage),
                                        color = VividGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            onDismiss()
                                            onOpenLowStock()
                                        }
                                    )
                                }
                            }
                        }
                        if (lowStockProducts.isEmpty()) {
                            item {
                                Text(
                                    text = ShopLocale.get("all_products_well_stocked", currentLanguage),
                                    color = TextMuted,
                                    fontSize = 12.5.sp
                                )
                            }
                        } else {
                            items(lowStockProducts.take(4), key = { "alert_low_${it.id}" }) { prod ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x14FFFFFF),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDismiss()
                                            onRestockProduct(prod)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WarningAmber,
                                                contentDescription = null,
                                                tint = if (prod.stock <= 0L) AlertRed else Color(0xFFFFB300),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = prod.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${ShopLocale.formatNumber(prod.stock, currentLanguage)} ${ShopLocale.displayUnit(prod.unit, currentLanguage)}",
                                            color = if (prod.stock <= 0L) AlertRed else Color(0xFFFFB300),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (canAccessBaki) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ShopLocale.format("baki_dues_count_header", currentLanguage, bakiOverview.debtorCount),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (bakiOverview.debtors.isNotEmpty()) {
                                    Text(
                                        text = ShopLocale.get("open_baki_link", currentLanguage),
                                        color = VividGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            onDismiss()
                                            onOpenBaki()
                                        }
                                    )
                                }
                            }
                        }
                        if (bakiOverview.debtors.isEmpty()) {
                            item {
                                Text(
                                    text = ShopLocale.get("no_pending_customer_dues", currentLanguage),
                                    color = TextMuted,
                                    fontSize = 12.5.sp
                                )
                            }
                        } else {
                            items(bakiOverview.debtors.take(4), key = { "alert_baki_${it.customer.id}" }) { debtor ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0x14FFFFFF),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDismiss()
                                            onOpenBaki()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = debtor.customer.name,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = ShopLocale.formatMoney(debtor.currentBalance, currentLanguage),
                                            color = VividGreen,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
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

@Composable
fun HomePaymentInDialog(
    storeViewModel: StoreViewModel,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val bakiOverview by storeViewModel.bakiOverview.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Pair<Customer, Double>?>(null) }

    if (selectedCustomer != null) {
        val (cust, balance) = selectedCustomer!!
        ReceivePaymentDialog(
            customer = cust,
            currentBalance = balance,
            currentLanguage = currentLanguage,
            onDismiss = { selectedCustomer = null },
            onConfirm = { amount, note, date ->
                storeViewModel.recordCustomerPayment(
                    customer = cust,
                    amount = amount,
                    note = note,
                    paymentDate = date,
                    onSuccess = {
                        selectedCustomer = null
                        onDismiss()
                    }
                )
            }
        )
        return
    }

    val filtered = remember(bakiOverview.debtors, search) {
        val q = search.trim()
        if (q.isEmpty()) bakiOverview.debtors
        else bakiOverview.debtors.filter {
            it.customer.name.contains(q, ignoreCase = true) || it.customer.mobile.contains(q, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ShopLocale.get("payment_in_title", currentLanguage),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SearchGlassField(
                    query = search,
                    onQueryChange = { search = it },
                    label = ShopLocale.get("search_due_customer", currentLanguage),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Text(
                        text = ShopLocale.get("no_customers_pending_baki", currentLanguage),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filtered.forEach { summary ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x18FFFFFF),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCustomer = summary.customer to summary.currentBalance }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.customer.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (summary.customer.mobile.isNotBlank()) {
                                            Text(
                                                text = summary.customer.mobile,
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Text(
                                        text = ShopLocale.formatMoney(summary.currentBalance, currentLanguage),
                                        color = VividGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePaymentOutDialog(
    storeViewModel: StoreViewModel,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val suppliers by storeViewModel.suppliers.collectAsState()
    val accounts by storeViewModel.accounts.collectAsState()
    val lastUsedAccountId by storeViewModel.lastUsedAccountId.collectAsState()
    val canCashBank by storeViewModel.canAccessCashAndBankFlow.collectAsState()

    var selectedSupplier by remember(suppliers) { mutableStateOf(suppliers.firstOrNull { it.balance > 0.0 } ?: suppliers.firstOrNull()) }
    var supplierDropdown by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val defaultAcc = accounts.find { it.id == lastUsedAccountId }
        ?: accounts.find { it.isDefault || it.id == "cash_in_hand" }
        ?: accounts.firstOrNull()
    var selectedAccId by remember { mutableStateOf(defaultAcc?.id ?: "cash_in_hand") }
    var selectedAccName by remember { mutableStateOf(defaultAcc?.name ?: "Cash in Hand") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ShopLocale.get("payment_out_title", currentLanguage),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (suppliers.isEmpty()) {
                    Text(
                        text = ShopLocale.get("no_suppliers_add_party_hint", currentLanguage),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassOutlinedButton(
                        text = ShopLocale.get("close", currentLanguage),
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = supplierDropdown,
                        onExpandedChange = { supplierDropdown = !supplierDropdown }
                    ) {
                        GlassTextField(
                            value = selectedSupplier?.let { "${it.name} (${ShopLocale.formatMoney(it.balance, currentLanguage)})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = ShopLocale.get("supplier_field_label", currentLanguage),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdown,
                            onDismissRequest = { supplierDropdown = false },
                            modifier = Modifier.background(DarkBackground)
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = ShopLocale.format(
                                                "supplier_due_option",
                                                currentLanguage,
                                                sup.name,
                                                ShopLocale.formatMoney(sup.balance, currentLanguage)
                                            ),
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        selectedSupplier = sup
                                        supplierDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMsg = null
                        },
                        label = ShopLocale.get("payment_amount_bdt", currentLanguage),
                        placeholder = ShopLocale.digits("0", currentLanguage),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if ((selectedSupplier?.balance ?: 0.0) > 0.0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val bal = selectedSupplier!!.balance
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x205DD62C),
                                border = BorderStroke(1.dp, VividGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        amountText = bal.toInt().toString()
                                        errorMsg = null
                                    }
                            ) {
                                Text(
                                    text = ShopLocale.format(
                                        "full_amount_chip",
                                        currentLanguage,
                                        ShopLocale.formatMoney(bal.toInt().toDouble(), currentLanguage)
                                    ),
                                    color = VividGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (canCashBank) {
                        AccountPickerField(
                            selectedAccountId = selectedAccId,
                            selectedAccountName = selectedAccName,
                            onAccountSelected = { acc ->
                                selectedAccId = acc.id
                                selectedAccName = acc.name
                            },
                            storeViewModel = storeViewModel,
                            label = ShopLocale.get("paid_from_account", currentLanguage)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    GlassTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = ShopLocale.get("note_optional", currentLanguage),
                        placeholder = ShopLocale.get("note_placeholder_settlement", currentLanguage),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg!!, color = AlertRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    VividButton(
                        text = ShopLocale.get("confirm_payment_out_btn", currentLanguage),
                        onClick = {
                            val sup = selectedSupplier
                            val amt = ShopLocale.toAsciiDigits(amountText).toDoubleOrNull() ?: 0.0
                            if (sup == null) {
                                errorMsg = ShopLocale.get("select_supplier_err", currentLanguage)
                                return@VividButton
                            }
                            if (amt <= 0.0) {
                                errorMsg = ShopLocale.get("enter_valid_amount_gt0", currentLanguage)
                                return@VividButton
                            }
                            storeViewModel.recordSupplierPayment(
                                supplier = sup,
                                amount = amt,
                                accountId = selectedAccId,
                                accountName = selectedAccName,
                                note = noteText,
                                date = System.currentTimeMillis(),
                                onSuccess = onDismiss
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRecordPurchaseDialog(
    storeViewModel: StoreViewModel,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val suppliers by storeViewModel.suppliers.collectAsState()
    val products by storeViewModel.products.collectAsState()
    val accounts by storeViewModel.accounts.collectAsState()
    val lastUsedAccountId by storeViewModel.lastUsedAccountId.collectAsState()
    val canCashBank by storeViewModel.canAccessCashAndBankFlow.collectAsState()

    var selectedSupplier by remember(suppliers) { mutableStateOf(suppliers.firstOrNull()) }
    var supplierDropdown by remember { mutableStateOf(false) }

    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }
    var productDropdown by remember { mutableStateOf(false) }
    var qtyText by remember { mutableStateOf("1") }
    var buyPriceText by remember(selectedProduct) {
        mutableStateOf(selectedProduct?.buyPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
    }

    val cartItems = remember { mutableStateListOf<PurchaseItem>() }
    val totalAmount = cartItems.sumOf { it.lineTotal }

    var paidAmountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val defaultAcc = accounts.find { it.id == lastUsedAccountId }
        ?: accounts.find { it.isDefault || it.id == "cash_in_hand" }
        ?: accounts.firstOrNull()
    var selectedAccId by remember { mutableStateOf(defaultAcc?.id ?: "cash_in_hand") }
    var selectedAccName by remember { mutableStateOf(defaultAcc?.name ?: "Cash in Hand") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = VividGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ShopLocale.get("record_purchase_title", currentLanguage),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (suppliers.isEmpty() || products.isEmpty()) {
                    Text(
                        text = if (suppliers.isEmpty()) {
                            ShopLocale.get("add_supplier_first_hint", currentLanguage)
                        } else {
                            ShopLocale.get("add_product_first_hint", currentLanguage)
                        },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassOutlinedButton(
                        text = ShopLocale.get("close", currentLanguage),
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = supplierDropdown,
                        onExpandedChange = { supplierDropdown = !supplierDropdown }
                    ) {
                        GlassTextField(
                            value = selectedSupplier?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = ShopLocale.get("supplier_field_label", currentLanguage),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdown,
                            onDismissRequest = { supplierDropdown = false },
                            modifier = Modifier.background(DarkBackground)
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text(sup.name, color = TextPrimary, maxLines = 1) },
                                    onClick = {
                                        selectedSupplier = sup
                                        supplierDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = productDropdown,
                        onExpandedChange = { productDropdown = !productDropdown }
                    ) {
                        GlassTextField(
                            value = selectedProduct?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = ShopLocale.get("product_field_label", currentLanguage),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = productDropdown,
                            onDismissRequest = { productDropdown = false },
                            modifier = Modifier.background(DarkBackground)
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${prod.name} (${ShopLocale.formatNumber(prod.stock, currentLanguage)} ${ShopLocale.displayUnit(prod.unit, currentLanguage)})",
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    },
                                    onClick = {
                                        selectedProduct = prod
                                        buyPriceText = if (prod.buyPrice > 0) prod.buyPrice.toInt().toString() else ""
                                        productDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        GlassTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = ShopLocale.get("qty", currentLanguage),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f)
                        )
                        GlassTextField(
                            value = buyPriceText,
                            onValueChange = { buyPriceText = it },
                            label = ShopLocale.get("buy_price_bdt_short", currentLanguage),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = VividGreen,
                            modifier = Modifier
                                .height(52.dp)
                                .clickable {
                                    val prod = selectedProduct ?: return@clickable
                                    val q = ShopLocale.toAsciiDigits(qtyText).toLongOrNull() ?: 0L
                                    val bp = ShopLocale.toAsciiDigits(buyPriceText).toDoubleOrNull() ?: 0.0
                                    if (q <= 0L || bp < 0.0) {
                                        errorMsg = ShopLocale.get("enter_valid_qty_gt0", currentLanguage)
                                        return@clickable
                                    }
                                    errorMsg = null
                                    cartItems.add(
                                        PurchaseItem(
                                            productId = prod.id,
                                            name = prod.name,
                                            qty = q,
                                            unit = prod.unit,
                                            buyPrice = bp,
                                            lineTotal = q * bp
                                        )
                                    )
                                    val newTotal = cartItems.sumOf { it.lineTotal }
                                    paidAmountText = newTotal.toInt().toString()
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = ShopLocale.get("add_item_cd", currentLanguage),
                                    tint = DarkBackground
                                )
                            }
                        }
                    }

                    if (cartItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x16FFFFFF),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                cartItems.forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.name} × ${ShopLocale.formatNumber(item.qty, currentLanguage)}",
                                            color = TextPrimary,
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = ShopLocale.formatMoney(item.lineTotal, currentLanguage),
                                            color = VividGreen,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        IconButton(
                                            onClick = {
                                                cartItems.removeAt(idx)
                                                paidAmountText = cartItems.sumOf { it.lineTotal }.toInt().toString()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = ShopLocale.get("remove_cd", currentLanguage),
                                                tint = AlertRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        ShopLocale.get("total_purchase_label", currentLanguage),
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        ShopLocale.formatMoney(totalAmount, currentLanguage),
                                        color = VividGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    GlassTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it },
                        label = ShopLocale.get("paid_now_credit_label", currentLanguage),
                        placeholder = ShopLocale.digits("0", currentLanguage),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (canCashBank) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AccountPickerField(
                            selectedAccountId = selectedAccId,
                            selectedAccountName = selectedAccName,
                            onAccountSelected = { acc ->
                                selectedAccId = acc.id
                                selectedAccName = acc.name
                            },
                            storeViewModel = storeViewModel,
                            label = ShopLocale.get("paid_from_account", currentLanguage)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    GlassTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = ShopLocale.get("note_optional", currentLanguage),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMsg!!, color = AlertRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    VividButton(
                        text = ShopLocale.get("save_purchase_btn", currentLanguage),
                        onClick = {
                            val sup = selectedSupplier
                            if (sup == null) {
                                errorMsg = ShopLocale.get("select_supplier_err", currentLanguage)
                                return@VividButton
                            }
                            if (cartItems.isEmpty()) {
                                errorMsg = ShopLocale.get("tap_plus_add_item_err", currentLanguage)
                                return@VividButton
                            }
                            val paid = (ShopLocale.toAsciiDigits(paidAmountText).toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalAmount)
                            storeViewModel.recordPurchase(
                                supplier = sup,
                                items = cartItems.toList(),
                                paidAmount = paid,
                                note = noteText,
                                accountId = selectedAccId,
                                accountName = selectedAccName,
                                onSuccess = { onDismiss() }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun HomeAddPartyDialog(
    storeViewModel: StoreViewModel,
    currentLanguage: AppLanguage,
    canAddCustomer: Boolean,
    canAddSupplier: Boolean,
    initialIsSupplier: Boolean? = null,
    onDismiss: () -> Unit
) {
    var isSupplier by remember(initialIsSupplier, canAddCustomer, canAddSupplier) {
        mutableStateOf(
            when {
                initialIsSupplier == true && canAddSupplier -> true
                initialIsSupplier == false && canAddCustomer -> false
                else -> !canAddCustomer && canAddSupplier
            }
        )
    }
    var nameInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var companyOrAddressInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = VividGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ShopLocale.get("add_party_title", currentLanguage),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = ShopLocale.get("close", currentLanguage),
                            tint = TextMuted
                        )
                    }
                }

                if (canAddCustomer && canAddSupplier) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isSupplier) Color(0x335DD62C) else Color(0x18FFFFFF),
                            border = BorderStroke(1.dp, if (!isSupplier) VividGreen else GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSupplier = false }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = ShopLocale.get("party_customer", currentLanguage),
                                    color = if (!isSupplier) VividGreen else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSupplier) Color(0x335DD62C) else Color(0x18FFFFFF),
                            border = BorderStroke(1.dp, if (isSupplier) VividGreen else GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSupplier = true }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = ShopLocale.get("party_supplier", currentLanguage),
                                    color = if (isSupplier) VividGreen else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                GlassTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        errorMsg = null
                    },
                    label = ShopLocale.get(if (isSupplier) "supplier_name" else "customer_name", currentLanguage),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassTextField(
                    value = mobileInput,
                    onValueChange = { mobileInput = it },
                    label = ShopLocale.get("store_phone", currentLanguage),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassTextField(
                    value = companyOrAddressInput,
                    onValueChange = { companyOrAddressInput = it },
                    label = ShopLocale.get(if (isSupplier) "company_name_opt" else "address_opt", currentLanguage),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = AlertRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                VividButton(
                    text = ShopLocale.get(if (isSupplier) "save_supplier_btn" else "save_customer", currentLanguage),
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isBlank()) {
                            errorMsg = ShopLocale.get("name_required_err", currentLanguage)
                            return@VividButton
                        }
                        if (isSupplier) {
                            storeViewModel.addSupplier(
                                name = trimmed,
                                mobile = mobileInput,
                                company = companyOrAddressInput,
                                address = "",
                                onSuccess = { onDismiss() }
                            )
                        } else {
                            storeViewModel.addCustomer(
                                name = trimmed,
                                mobile = mobileInput,
                                address = companyOrAddressInput,
                                notes = "",
                                onSuccess = { onDismiss() }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
