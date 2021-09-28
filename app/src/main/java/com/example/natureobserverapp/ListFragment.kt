package com.example.natureobserverapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListFragment : Fragment() {

    private lateinit var adapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(
        view: View, savedInstanceState:
        Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_obs_list)
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        val ump: NatureObservationModel by viewModels()
        ump.getNatureObservations().observe(this) {
            recyclerView.adapter = RecyclerViewAdapter(it?.sortedBy { that ->
                that.title
            })
        }
    }
}