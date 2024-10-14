package com.yushin.flux_lock.store

import com.yushin.flux_lock.action.ToDoAction
import com.yushin.flux_lock.dispatcher.ToDoDispatcher
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 * アプリケーションの状態管理をする
 */
object TodoStore {
    private val todos = mutableListOf<Todo>()
    private val todoSubject = BehaviorSubject.createDefault(todos)
    private val disposables = CompositeDisposable()

    init {
        val disposable = ToDoDispatcher.onAction()
            .subscribe { action ->
            when (action) {
                is ToDoAction.LoadTodos -> loadTodos()
                is ToDoAction.AddTodo -> addTodo(action.title)
                is ToDoAction.ToggleTodo -> toggleTodo(action.id)
                is ToDoAction.DeleteTodo -> deleteTodo(action.id)
            }
        }
        disposables.add(disposable)
    }

    fun onDestroy(){
        disposables.clear()
    }

    /**
     * Todoリストのゲッター
     */
    fun getTodos(): BehaviorSubject<MutableList<Todo>> = todoSubject

    private fun loadTodos() {
        todoSubject.onNext(todos)
    }

    private fun addTodo(title: String) {
        val newTodo = Todo(
            id = (todos.maxOfOrNull { it.id } ?: 0) + 1,
            title = title
        )
        todos.add(newTodo)
        todoSubject.onNext(todos)
    }

    private fun toggleTodo(id: Int) {
        val todo = todos.find { it.id == id }
        todo?.let {
            it.isCompleted = !it.isCompleted
            todoSubject.onNext(todos)
        }
    }

    private fun deleteTodo(id: Int) {
        todos.removeAll { it.id == id }
        todoSubject.onNext(todos)
    }
}

data class Todo(
    val id: Int,
    val title: String,
    var isCompleted: Boolean = false
)