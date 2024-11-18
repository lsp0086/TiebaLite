package com.huanchengfly.tieba.post.ui.page.main.explore

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.placeholder.placeholder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.SearchPageDestination
import com.huanchengfly.tieba.post.ui.page.main.home.EmptyScreen
import com.huanchengfly.tieba.post.ui.page.main.home.SearchBox
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.ConfirmDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.ErrorScreen
import com.huanchengfly.tieba.post.ui.widgets.compose.HorizontalDivider
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.MenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyVerticalGrid
import com.huanchengfly.tieba.post.ui.widgets.compose.MyScaffold
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.ui.widgets.compose.states.StateScreen
import com.huanchengfly.tieba.post.utils.AccountUtil.LocalAccount
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import kotlinx.collections.immutable.persistentListOf
import java.text.DecimalFormat

fun RGBA(red:Int,green:Int,blue:Int,alpha:Float):Color{
    return Color(red,green,blue,(255 * alpha).toInt())
}
fun <T> List<T>.customSort(comparator: (T, T) -> Boolean): List<T> {
    val sortedList = this.toMutableList()
    for (i in 0 until sortedList.size - 1) {
        for (j in i + 1 until sortedList.size) {
            if (comparator(sortedList[i], sortedList[j])) {
                val temp = sortedList[i]
                sortedList[i] = sortedList[j]
                sortedList[j] = temp
            }
        }
    }
    return sortedList
}
@Immutable
data class ExplorePageItem(
    val id: String,
    val name: @Composable (selected: Boolean) -> Unit,
    val content: @Composable () -> Unit,
)
val DiamondShape = GenericShape { size: Size, _ ->
    val path = Path().apply {
        // 上半部分梯形
        moveTo(0f, size.height * 0.48f)
        lineTo(size.width * 0.3f, size.height * 0.18f) // 顶点
        lineTo(size.width * 0.7f, size.height * 0.18f) // 右上角
        lineTo(size.width, size.height * 0.48f) // 右下角

        // 下半部分三角形
        lineTo(size.width * 0.5f, size.height) // 底部中心
        close() // 闭合路径
    }
    addPath(path)
}
@Composable
private fun ForumItemContent(
    item: ExploreUiState.Forum
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = CenterVertically
    ) {
        Row {
            Avatar(data = item.avatar, size = 32.dp, contentDescription = null, shape = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column (modifier = Modifier.weight(1f).fillMaxHeight(),verticalArrangement = Arrangement.Center) {
            Text(
                color = Color.Black,
                text = item.forumName,
                modifier = Modifier.fillMaxHeight(0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            HorizontalDivider(Modifier.height(5.dp))
            Text(
                color = RGBA(141,141,141,1f),
                text = buildString {
                    append("热度 ")
                    append(
                        if (item.hotNum > 10000){
                            "${DecimalFormat("#.##").format(item.hotNum.toDouble() / 10000)}W"
                        }else{
                            "${item.hotNum}"
                        }
                    )
                },
                modifier = Modifier.fillMaxHeight(0.35f),
                fontSize = 10.sp,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        //11,220,180
        //120,175,255
        //250,210,50
        //240,155,30
        val intLvId = item.levelId.toIntOrNull() ?: 0
        val boxColor = if (intLvId < 4){
            Color(11,220,180,255)
        }else if (intLvId < 10){
            Color(120,175,255,255)
        }else if (intLvId < 16){
            Color(250,210,50,255,)
        }else{
            Color(240,155,30,255)
        }
        Box(Modifier
            .background(
                color = boxColor,
                shape = DiamondShape,
            )
            .size(18.dp)
            .padding(vertical = 4.dp)
            .align(CenterVertically))
        {
            Text(
                text = item.levelId,
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Center)
            )
        }
    }
}

@Composable
private fun ForumItemMenuContent(
    menuState: MenuState,
    isTopForum: Boolean,
    onDeleteTopForum: () -> Unit,
    onAddTopForum: () -> Unit,
    onCopyName: () -> Unit,
    onUnfollow: () -> Unit,
) {
    DropdownMenuItem(
        onClick = {
            if (isTopForum) {
                onDeleteTopForum()
            } else {
                onAddTopForum()
            }
            menuState.expanded = false
        }
    ) {
        if (isTopForum) {
            Text(text = stringResource(id = R.string.menu_top_del))
        } else {
            Text(text = stringResource(id = R.string.menu_top))
        }
    }
    DropdownMenuItem(
        onClick = {
            onCopyName()
            menuState.expanded = false
        }
    ) {
        Text(text = stringResource(id = R.string.title_copy_forum_name))
    }
    DropdownMenuItem(
        onClick = {
            onUnfollow()
            menuState.expanded = false
        }
    ) {
        Text(text = stringResource(id = R.string.button_unfollow))
    }
}

@Composable
private fun ForumItem(
    item: ExploreUiState.Forum,
    onClick: (ExploreUiState.Forum) -> Unit,
    onUnfollow: (ExploreUiState.Forum) -> Unit,
    onAddTopForum: (ExploreUiState.Forum) -> Unit,
    onDeleteTopForum: (ExploreUiState.Forum) -> Unit,
    isTopForum: Boolean = false,
) {
    val context = LocalContext.current
    val menuState = rememberMenuState()
    LongClickMenu(
        menuContent = {
            ForumItemMenuContent(
                menuState = menuState,
                isTopForum = isTopForum,
                onDeleteTopForum = { onDeleteTopForum(item) },
                onAddTopForum = { onAddTopForum(item) },
                onCopyName = {
                    TiebaUtil.copyText(context, item.forumName)
                },
                onUnfollow = { onUnfollow(item) }
            )
        },
        menuState = menuState,
        onClick = {
            onClick(item)
        }
    ) {
        ForumItemContent(item = item)
    }
}

private fun getGridCells(context: Context, listSingle: Boolean = context.appPreferences.listSingle): GridCells {
    return if (listSingle) {
        GridCells.Fixed(1)
    } else {
        GridCells.Adaptive(180.dp)
    }
}

@Composable
private fun Header(
    text: String,
    modifier: Modifier = Modifier,
    invert: Boolean = false
) {
    Chip(
        text = text,
        modifier = Modifier
            .padding(start = 16.dp)
            .then(modifier),
        invertColor = invert
    )
}

@Composable
private fun ForumItemPlaceholder(
    showAvatar: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (showAvatar) {
            Image(
                painter = rememberDrawablePainter(
                    drawable = ImageUtil.getPlaceHolder(
                        LocalContext.current,
                        0
                    )
                ),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
                    .align(CenterVertically)
                    .placeholder(visible = true, color = ExtendedTheme.colors.chip),
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Text(
            color = ExtendedTheme.colors.text,
            text = "",
            modifier = Modifier
                .weight(1f)
                .align(CenterVertically)
                .placeholder(visible = true, color = ExtendedTheme.colors.chip),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(54.dp)
                .background(
                    color = ExtendedTheme.colors.chip,
                    shape = RoundedCornerShape(3.dp)
                )
                .padding(vertical = 4.dp)
                .align(CenterVertically)
                .placeholder(visible = true, color = ExtendedTheme.colors.chip)
        ) {
            Text(
                text = "0",
                color = ExtendedTheme.colors.onChip,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Center)
            )
        }
    }
}


@Composable
private fun ExploreSkeletonScreen(
    listSingle: Boolean,
    gridCells: GridCells
) {
    MyLazyVerticalGrid(
        columns = gridCells,
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item(key = "TopForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Header(
                    text = stringResource(id = R.string.title_top_forum),
                    modifier = Modifier.placeholder(
                        visible = true,
                        color = ExtendedTheme.colors.chip
                    ),
                    invert = true
                )
            }
        }
        items(6, key = { "TopPlaceholder$it" }) {
            ForumItemPlaceholder(listSingle)
        }
        item(
            key = "Spacer",
            span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                modifier = Modifier.height(
                    16.dp
                )
            )
        }
        item(key = "ForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Header(
                    text = stringResource(id = R.string.forum_list_title),
                    modifier = Modifier.placeholder(
                        visible = true,
                        color = ExtendedTheme.colors.chip
                    ),
                    invert = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(12, key = { "Placeholder$it" }) {
            ForumItemPlaceholder(listSingle)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun ExplorePage(
    viewModel: ExploreViewModel = pageViewModel<ExploreUiIntent, ExploreViewModel>(listOf(
        ExploreUiIntent.Refresh)),
    canOpenExplore: Boolean = false,
    onOpenExplore: () -> Unit = {},
) {
    val account = LocalAccount.current
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val isLoading by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::isLoading,
        initial = true
    )
    val forums by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::forums,
        initial = persistentListOf()
    )
    val topForums by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::topForums,
        initial = persistentListOf()
    )
    val historyForums by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::historyForums,
        initial = persistentListOf()
    )
    val expandHistoryForum by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::expandHistoryForum,
        initial = true
    )
    val showFollowedType by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::followedType,
        initial = 0
    )

    val showAllFollowed by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::showAll,
        initial = false
    )

    val error by viewModel.uiState.collectPartialAsState(
        prop1 = ExploreUiState::error,
        initial = null
    )
    val isLoggedIn = remember(account) { account != null }
    val isEmpty by remember { derivedStateOf { forums.isEmpty() } }
    val hasTopForum by remember { derivedStateOf { topForums.isNotEmpty() } }
    val showHistoryForum by remember { derivedStateOf { context.appPreferences.explorePageShowHistoryForum && historyForums.isNotEmpty() } }
    val listSingle by remember { mutableStateOf(context.appPreferences.listSingle) }
    val isError by remember { derivedStateOf { error != null } }
    val gridCells by remember { derivedStateOf { getGridCells(context, listSingle) } }

    onGlobalEvent<GlobalEvent.Refresh>(
        filter = { it.key == "home" }
    ) {
        viewModel.send(ExploreUiIntent.Refresh)
    }

    var unfollowForum by remember { mutableStateOf<ExploreUiState.Forum?>(null) }
    val confirmUnfollowDialog = rememberDialogState()
    ConfirmDialog(
        dialogState = confirmUnfollowDialog,
        onConfirm = {
            unfollowForum?.let {
                viewModel.send(ExploreUiIntent.Unfollow(it.forumId, it.forumName))
            }
        },
    ) {
        Text(
            text = stringResource(
                id = R.string.title_dialog_unfollow_forum,
                unfollowForum?.forumName.orEmpty()
            )
        )
    }

    LaunchedEffect(Unit) {
        if (viewModel.initialized) viewModel.send(ExploreUiIntent.RefreshHistory)
    }

    MyScaffold(
        backgroundColor = Color.Transparent,
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.title_explore),
                actions = {
                    ActionItem(
                        icon = ImageVector.vectorResource(id = R.drawable.ic_oksign),
                        contentDescription = stringResource(id = R.string.title_oksign)
                    ) {
                        TiebaUtil.startSign(context)
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { contentPaddings ->
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isLoading,
            onRefresh = { viewModel.send(ExploreUiIntent.Refresh) }
        )
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .padding(contentPaddings)
        ) {
            Column {
                SearchBox(modifier = Modifier.padding(bottom = 4.dp)) {
                    navigator.navigate(SearchPageDestination)
                }
                StateScreen(
                    isEmpty = isEmpty,
                    isError = isError,
                    isLoading = isLoading,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    onReload = {
                        viewModel.send(ExploreUiIntent.Refresh)
                    },
                    emptyScreen = {
                        EmptyScreen(
                            loggedIn = isLoggedIn,
                            canOpenExplore = canOpenExplore,
                            onOpenExplore = onOpenExplore
                        )
                    },
                    loadingScreen = {
                        ExploreSkeletonScreen(listSingle = listSingle, gridCells = gridCells)
                    },
                    errorScreen = {
                        error?.let { ErrorScreen(error = it) }
                    }
                ) {
                    MyLazyVerticalGrid(
                        columns = gridCells,
                        contentPadding = PaddingValues(bottom = 12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (showHistoryForum) {
                            item(key = "HistoryForums", span = { GridItemSpan(maxLineSpan) }) {
                                val rotate by animateFloatAsState(
                                    targetValue = if (expandHistoryForum) 90f else 0f,
                                    label = "rotate"
                                )
                                Column {
                                    Row(
                                        verticalAlignment = CenterVertically,
                                        modifier = Modifier
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                viewModel.send(
                                                    ExploreUiIntent.ToggleHistory(
                                                        expandHistoryForum
                                                    )
                                                )
                                            }
                                            .padding(vertical = 8.dp)
                                            .padding(end = 16.dp)
                                    ) {
                                        Header(
                                            text = stringResource(id = R.string.title_history_forum),
                                            invert = false
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = stringResource(id = R.string.desc_show),
                                            modifier = Modifier
                                                .size(24.dp)
                                                .rotate(rotate)
                                        )
                                    }
                                    AnimatedVisibility(visible = expandHistoryForum) {
                                        LazyRow(
                                            contentPadding = PaddingValues(bottom = 8.dp),
                                        ) {
                                            item(key = "Spacer1") {
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                            items(
                                                historyForums,
                                                key = { it.data }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .padding(horizontal = 4.dp)
                                                        .height(IntrinsicSize.Min)
                                                        .clip(RoundedCornerShape(100))
                                                        .background(color = ExtendedTheme.colors.chip)
                                                        .clickable {
                                                            navigator.navigate(
                                                                ForumPageDestination(
                                                                    it.data
                                                                )
                                                            )
                                                        }
                                                        .padding(4.dp),
                                                    verticalAlignment = CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Avatar(
                                                        data = it.avatar,
                                                        contentDescription = null,
                                                        size = 24.dp,
                                                        shape = CircleShape
                                                    )
                                                    Text(
                                                        text = it.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    )
                                                }
                                            }
                                            item(key = "Spacer2") {
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (hasTopForum) {
                            item(key = "TopForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Header(
                                        text = stringResource(id = R.string.title_top_forum),
                                        invert = true
                                    )
                                }
                            }
                            items(
                                items = topForums,
                                key = { "Top${it.forumId}" }
                            ) { item ->
                                ForumItem(
                                    item,
                                    onClick = {
                                        navigator.navigate(ForumPageDestination(it.forumName))
                                    },
                                    onUnfollow = {
                                        unfollowForum = it
                                        confirmUnfollowDialog.show()
                                    },
                                    onAddTopForum = {
                                        viewModel.send(ExploreUiIntent.TopForums.Add(it))
                                    },
                                    onDeleteTopForum = {
                                        viewModel.send(ExploreUiIntent.TopForums.Delete(it.forumId))
                                    },
                                    isTopForum = true
                                )
                            }
                        }
                        item(key = "ForumHeader", span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = CenterVertically
                            ) {
                                Header(text = stringResource(id = R.string.forum_list_title))
                                Row(Modifier.weight(1f)){}
                                val iconPainter = if (showFollowedType == 0) painterResource(R.drawable.tieba_default_sort) else painterResource(R.drawable.tieba_level_sort)
                                Image(
                                    painter =  iconPainter,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Row(Modifier.width(3.dp)) {  }
                                Text(text = if (showFollowedType == 0) "默认排序" else "等级排序",
                                    fontSize = 12.sp,
                                    color = RGBA(168,168,168,1f),
                                    modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null)
                                {
                                        viewModel.send(
                                            ExploreUiIntent.ChangeFollowedType(showFollowedType)
                                        )
                                })
                                Row(Modifier.width(12.dp)) {  }
                            }
                        }
                        item (key = "Followed") {
                            var typeForums = forums.toList()
                            if (showFollowedType == 1){
                                typeForums = forums.customSort{ a,b ->
                                    val aInt = a.levelId.toIntOrNull() ?: 0
                                    val bInt = b.levelId.toIntOrNull() ?: 0
                                    aInt >= bInt
                                }
                            }
                            if (!showAllFollowed){
                                if (typeForums.size > 20) {
                                    typeForums = typeForums.subList(0,20)
                                }
                            }
                            FlowRow (Modifier.fillMaxWidth()) {
                                typeForums.forEach{ item ->
                                    ForumItem(
                                        item,
                                        onClick = {
                                            navigator.navigate(ForumPageDestination(it.forumName))
                                        },
                                        onUnfollow = {
                                            unfollowForum = it
                                            confirmUnfollowDialog.show()
                                        },
                                        onAddTopForum = {
                                            viewModel.send(ExploreUiIntent.TopForums.Add(it))
                                        },
                                        onDeleteTopForum = {
                                            viewModel.send(ExploreUiIntent.TopForums.Delete(it.forumId))
                                        }
                                    )
                                }
                                if (forums.size > 20){
                                    Row (
                                        modifier =  Modifier.fillMaxWidth().height(44.dp).clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null) {
                                            viewModel.send(
                                                ExploreUiIntent.ShowAllFollowed(showAllFollowed)
                                            )
                                        },
                                        verticalAlignment = CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                        ) {
                                        Text(
                                            text = buildString {
                                                append(if (showAllFollowed) "收起列表" else "展示列表")
                                                append(" " + if (showAllFollowed) "︿" else "﹀")
                                            },
                                            fontSize = 12.sp,
                                            color = RGBA(168,168,168,1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
                contentColor = ExtendedTheme.colors.primary,
            )
        }
    }
}