package org.d3if3157.assessment3.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if3157.assessment3.BuildConfig
import org.d3if3157.assessment3.R
import org.d3if3157.assessment3.model.Galeri
import org.d3if3157.assessment3.model.User
import org.d3if3157.assessment3.network.ApiStatus
import org.d3if3157.assessment3.network.GaleriApi
import org.d3if3157.assessment3.network.UserDataStore
import org.d3if3157.assessment3.util.SettingsDataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    val settingDataStore = SettingsDataStore(LocalContext.current)
    val showList by settingDataStore.layoutFlow.collectAsState(true)
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())
    var showDialog by remember {
        mutableStateOf(false)
    }
    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage
    var showGaleriDialog by remember { mutableStateOf(false) }
    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }
    val launcher = rememberLauncherForActivityResult(CropImageContract()){
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showGaleriDialog = true
    }
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Row {
                        IconButton(
                            onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                settingDataStore.saveLayout(!showList)
                            }
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (showList) R.drawable.baseline_grid_view_24
                                    else R.drawable.baseline_view_list_24
                                ),
                                contentDescription = stringResource(
                                    if (showList) R.string.grid
                                    else R.string.list
                                ),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {
                            if (user.email.isEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                            }
                            else {
                                showDialog = true
    //                            Log.d("SIGN-IN", "User: $user")
                            }
                        }) {
                            Icon(painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                contentDescription = stringResource(id = R.string.profil),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val option = CropImageContractOptions(
                    null, CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = true,
                        fixAspectRatio = true
                    )
                )
                launcher.launch(option)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.tambah_galeri))
            }
        }
    ){
            padding -> ScreenContent(showList, viewModel,user.email, Modifier.padding(padding), user.email)
        if (showDialog){
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }
        if (showGaleriDialog){
            GaleriDialog(
                bitmap = bitmap,
                onDismissRequest = { showGaleriDialog = false }
            ){
                    namaLengkap, deskripsi -> viewModel.postGaleri("",namaLengkap, deskripsi, "", user.email)
                showGaleriDialog = false
            }
        }
        if (errorMessage != null){
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(showList: Boolean, viewModel: MainViewModel, userId: String, modifier : Modifier, userEmail: String){
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId){
        viewModel.retrieveData(userId)
    }
    when (status){
        ApiStatus.LOADING -> {
            Box (
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
//                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {
            if (showList){
                LazyVerticalGrid(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ){
                    items(data) { GridItem(galeri = it, userId, userEmail)}
                }
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 84.dp)){
                    items(data){
                        ListItem(galeri = it, userId, userEmail)
                    }
                }
            }
        }
        ApiStatus.FAILED -> {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }

}

@Composable
fun GridItem(galeri: Galeri, userId: String, userEmail: String){
    var showDialogDelete by remember {
        mutableStateOf(false)
    }
    val viewModel: MainViewModel = viewModel()
    if (galeri.mine.equals(userEmail) || galeri.mine.equals("0")){
        Box(
            modifier = Modifier
                .padding(4.dp)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.BottomCenter
        ){
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(GaleriApi.getHewanUrl(galeri.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.gambar, galeri.namaLengkap),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.loading_img),
                error = painterResource(id = R.drawable.baseline_broken_image_24),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Column {
                        Text(
                            text = galeri.namaLengkap,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = galeri.deskripsi,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    if (!userId.equals("") && !galeri.mine.equals("0")){
                        IconButton(onClick = {
                            showDialogDelete = true;
                        } ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            if (showDialogDelete){
                                DeleteDialog(
                                    onDismissRequest = { showDialogDelete = false}) {
                                    CoroutineScope(Dispatchers.IO).launch { viewModel.deleteImage(userId, galeri.id) }
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
fun ListItem(galeri: Galeri, userId: String, userEmail: String){
    var showDialogDelete by remember {
        mutableStateOf(false)
    }
    val viewModel: MainViewModel = viewModel()
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if (galeri.mine.equals(userEmail) || galeri.mine.equals("0")){
            Row (
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Column {
                    Text(text = galeri.namaLengkap, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Text(text = galeri.deskripsi, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (!userId.equals("") && !galeri.mine.equals("0")){
                    IconButton(onClick = {
                        showDialogDelete = true;
                    } ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        if (showDialogDelete){
                            DeleteDialog(
                                onDismissRequest = { showDialogDelete = false}) {
                                CoroutineScope(Dispatchers.IO).launch { viewModel.deleteImage(userId, galeri.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}
private suspend fun signIn(context: Context, dataStore: UserDataStore){
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()
    val  request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException){
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
){
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
//            Log.d("SIGN-IN", "User email: ${googleId.id}")
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unreconigzed custom credential type")
    }
}
private suspend fun signOut(context: Context, dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        dataStore.saveData(User())
    } catch (e: ClearCredentialException){
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}
private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful){
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val uri = result.uriContent ?: return null
    return if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else{
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}
