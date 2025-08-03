package de.rafaelbeckmann.hvkclient.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun <T> LazyListScope.roundedListItems(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    onItemClick: ((T) -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    innerRadius: Dp = 4.dp,
    itemContent: @Composable (item: T) -> Unit
) {
    items.forEachIndexed { index, item ->
        val isFirst = index == 0
        val isLast = index == items.lastIndex
        val shape = RoundedCornerShape(
            topStart = if (isFirst) cornerRadius else innerRadius,
            topEnd = if (isFirst) cornerRadius else innerRadius,
            bottomStart = if (isLast) cornerRadius else innerRadius,
            bottomEnd = if (isLast) cornerRadius else innerRadius
        )

        item(key = key?.invoke(item)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .animateItem(),
                onClick = { onItemClick?.invoke(item) },
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                itemContent(item)
            }
        }
    }
}

@Composable
fun RoundedListItem(
    modifier: Modifier = Modifier,
    text: String,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )
        trailingIcon?.invoke()
    }
}