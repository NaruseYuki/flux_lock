package com.yushin.flux_lock.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("NameList", Context.MODE_PRIVATE)
    private val nameSetKey = "names"

    // 名前リストを取得
    fun getNames(): List<String> {
        val nameSet = sharedPreferences.getStringSet(nameSetKey, setOf()) ?: setOf()
        return nameSet.toList()
    }

    // 名前を追加
    fun addName(name: String) {
        val currentNames = getNames().toMutableSet()
        currentNames.add(name)
        saveName(currentNames)
    }

    // 名前を削除
    fun removeName(name: String) {
        val currentNames = getNames().toMutableSet()
        currentNames.remove(name)
        saveName(currentNames)
    }

    // 名前を編集
    fun editName(oldName: String, newName: String) {
        val currentNames = getNames().toMutableSet()
        currentNames.remove(oldName)
        currentNames.add(newName)
        saveName(currentNames)
    }

    // 名前リストを保存
    private fun saveName(names: Set<String>) {
        sharedPreferences.edit().putStringSet(nameSetKey, names).apply()
    }

    // 名前リストをクリア
    fun clearNames() {
        sharedPreferences.edit().remove(nameSetKey).apply()
    }
}