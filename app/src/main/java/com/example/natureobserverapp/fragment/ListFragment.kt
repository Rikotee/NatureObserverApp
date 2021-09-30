package com.example.natureobserverapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natureobserverapp.NatureObservationsWithWeatherInfoModel
import com.example.natureobserverapp.R
import com.example.natureobserverapp.RecyclerViewAdapter

class ListFragment : Fragment(), RecyclerViewAdapter.ClickListener {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.list_title_text)

        val observationsRecyclerView = view.findViewById<RecyclerView>(R.id.rv_obs_list)
        observationsRecyclerView.layoutManager = LinearLayoutManager(this.context)
        val nowwim: NatureObservationsWithWeatherInfoModel by viewModels()
        nowwim.getNatureObservationsWithWeatherInfo().observe(this) {
            observationsRecyclerView.adapter = RecyclerViewAdapter(it, this)
        }
    }

    override fun onItemClick(observation: Long?) {

        Log.d("DBG", "onItemClick id $observation")

        val bundle = bundleOf("pos" to observation)

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ItemFragment>(R.id.flFragment, args = bundle)
            addToBackStack(null)
        }
    }
}