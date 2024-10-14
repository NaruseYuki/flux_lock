package com.yushin.flux_lock.action

/**
 * アプリ内のアクションを定義するクラス
 */

sealed class ToDoAction {
    // シングルトンにより、インスタンスが1つしか作成されない
    // タスクを読み込む
    data object LoadTodos:ToDoAction()
    // タスク追加
    data class AddTodo(val title:String):ToDoAction()
    // タスク変更
    data class ToggleTodo(val id:Int):ToDoAction()
    // タスク削除
    data class DeleteTodo(val id:Int):ToDoAction()
}