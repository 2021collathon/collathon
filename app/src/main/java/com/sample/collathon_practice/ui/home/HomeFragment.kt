package com.sample.collathon_practice.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sample.collathon_practice.R
import com.sample.collathon_practice.TimeCapsule
import com.sample.collathon_practice.databinding.ActivityMainBinding
import com.sample.collathon_practice.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.item_timecapsule.view.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    val db = Firebase.firestore
    var user_family=""
    var family_name=""
    
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        //get family info
        val user = Firebase.auth.currentUser
        if (user != null) {
            db?.collection("users").document(user.uid).get()
                .addOnSuccessListener { result->
                    user_family=result.data?.get("family_id").toString().trim()
                    db?.collection("family").document(user_family).get()
                        .addOnSuccessListener { result->
                            family_name=result.data?.get("name").toString().trim()
                            var familyname=binding.familyname
                            familyname.text=family_name

                            var recyclerView:RecyclerView=binding.homeRecyclerview
                            recyclerView.adapter = RecyclerViewAdapter()
                            recyclerView.layoutManager = LinearLayoutManager(getActivity())
                        }
                }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var timecapsule : ArrayList<TimeCapsule> = arrayListOf()

        init {
            db?.collection("family").document(user_family)
                .collection("posts").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // Clear ArrayList
                timecapsule.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(TimeCapsule::class.java)
                    timecapsule.add(item!!)
                }
                notifyDataSetChanged()
            }
        }

        // item_tiimecapsule.xml을 inflate
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_timecapsule, parent, false)
            return ViewHolder(view)
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        // connect view with real data
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as ViewHolder).itemView

            viewHolder.view_title.text = timecapsule[position]?.title
            viewHolder.view_time.text=timecapsule[position]?.time+" 까지"
        }

        override fun getItemCount(): Int { // size
            return timecapsule.size
        }
    }
}