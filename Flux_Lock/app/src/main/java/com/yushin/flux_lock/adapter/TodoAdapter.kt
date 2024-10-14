package com.yushin.flux_lock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.R
import com.yushin.flux_lock.store.Todo

class TodoAdapter(
    private var todos: MutableList<Todo>,
    private val todoClickListener: (Todo) -> Unit,
    private val todoDeleteListener: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todos[position], todoClickListener, todoDeleteListener)
    }

    override fun getItemCount(): Int = todos.size

    fun updateTodos(newTodos: List<Todo>) {
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.todoTitle)
        private val checkBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.todoDeleteButton)

        fun bind(todo: Todo, clickListener: (Todo) -> Unit, deleteListener: (Todo) -> Unit) {
            titleTextView.text = todo.title
            checkBox.isChecked = todo.isCompleted
            itemView.setOnClickListener { clickListener(todo) }
            deleteButton.setOnClickListener { deleteListener(todo) }
        }
    }
}
