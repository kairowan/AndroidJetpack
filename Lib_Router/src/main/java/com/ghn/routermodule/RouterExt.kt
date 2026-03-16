package com.ghn.routermodule

import com.therouter.TheRouter
import com.therouter.router.Navigator

inline fun String.navigate(block: Navigator.() -> Unit = {}) {
    TheRouter.build(this)
        .apply(block)
        .navigation()
}
