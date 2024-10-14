package com.yushin.flux_lock.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yushin.flux_lock.R
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.action.ToDoAction
import com.yushin.flux_lock.adapter.TodoAdapter
import com.yushin.flux_lock.dispatcher.ToDoDispatcher
import com.yushin.flux_lock.store.Todo
import com.yushin.flux_lock.store.TodoStore
import io.reactivex.rxjava3.disposables.CompositeDisposable

class TodoFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var todoAdapter: TodoAdapter
    private val disposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todoAdapter = TodoAdapter(mutableListOf(), ::onTodoClicked, ::onTodoDeleted)

        this.recyclerView = view.findViewById<RecyclerView>(R.id.container_recycler_view).apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showAddTodoDialog()
        }

        // TodoStoreを監視し、変更があったらUIを更新する
        TodoStore.getTodos().skip(1).subscribe{ todos ->
            todoAdapter.updateTodos(todos)
        }.addTo(disposable)

        // 初期データをロードする
        ToDoDispatcher.dispatch(ToDoAction.LoadTodos)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.recyclerView?.adapter = null
        this.recyclerView = null
        TodoStore.onDestroy()
        disposable.dispose()
    }


    private fun onTodoClicked(todo: Todo) {
        ToDoDispatcher.dispatch(ToDoAction.ToggleTodo(todo.id))
    }

    private fun onTodoDeleted(todo: Todo) {
        ToDoDispatcher.dispatch(ToDoAction.DeleteTodo(todo.id))
    }

    private fun showAddTodoDialog() {
        val editText = EditText(context)
        AlertDialog.Builder(context)
            .setTitle("Add Todo")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val title = editText.text.toString()
                if (title.isNotBlank()) {
                    ToDoDispatcher.dispatch(ToDoAction.AddTodo(title))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}