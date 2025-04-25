package com.example.yourfinance.domain.model

import com.example.yourfinance.util.StringHelper.Companion.getUpperFirstChar
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DelegateUpperCaseFirstChar(initialValue: String) : ReadWriteProperty<Any?, String> {
    private var _value : String = getUpperFirstChar(initialValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return _value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        _value = getUpperFirstChar(value)
    }
}