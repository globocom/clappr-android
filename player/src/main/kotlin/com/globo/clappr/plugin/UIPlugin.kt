package com.globo.clappr.plugin

import com.globo.clappr.base.BaseObject
import com.globo.clappr.base.EventInterface
import com.globo.clappr.base.UIObject

enum class PluginVisibility { HIDDEN, VISIBLE }

abstract class UIPlugin (component: BaseObject, private val uiObject: UIObject = UIObject()) : Plugin(component), EventInterface by uiObject {
    var visibility = PluginVisibility.HIDDEN
}