package com.example.services

import android.app.Notification
import android.graphics.Color
import android.graphics.Color.green
import android.os.Bundle
import android.service.autofill.VisibilitySetterAction
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.services.models.ChatMessage
import com.example.services.models.Invitation
import com.example.services.shared.currentUser
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_call_worker.*
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.notification_tile_client.view.*


class Notification_fragment : Fragment() {
    companion object {
        fun newInstance() : Notification_fragment = Notification_fragment()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_notification, container, false)

    private val adaptor = GroupAdapter<ViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notification_recyclerview.adapter = adaptor
        loadNotifications()
    }

    private fun loadNotifications(){
        val ref = FirebaseDatabase.getInstance().getReference("/invitations/${currentUser!!.uid}")
        ref.keepSynced(true)
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val ref2 = ref.child(snapshot.key.toString())
                ref2.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val invitation = snapshot.getValue(Invitation::class.java)
                        adaptor.add(LatestNotice(invitation!!,ref2,snapshot.key.toString()))
                       // Log.d("Logs","$ref ---> ${snapshot.key}")
                    }
                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    }
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }
                    override fun onChildRemoved(snapshot: DataSnapshot) {
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    class LatestNotice(val invitation: Invitation, var ref:DatabaseReference, val snapshotKey:String): Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {

            ref=ref.child(snapshotKey)

            viewHolder.itemView.notif_type.text = "Type: "+invitation.type
            viewHolder.itemView.notif_time.text = invitation.time
            viewHolder.itemView.notif_btn.text = invitation.status
            if(invitation.clientID== currentUser?.uid){
                viewHolder.itemView.notif_btn.isEnabled = false
                viewHolder.itemView.notif_title.text = "You invited "+invitation.workerName

                viewHolder.itemView.notif_reject_btn.isEnabled = false
                viewHolder.itemView.notif_reject_btn.visibility = View.GONE

            }else{
                viewHolder.itemView.notif_title.text = invitation.clientName+" invited you"
                if(invitation.status=="Pending"){
                    viewHolder.itemView.notif_reject_btn.setBackgroundColor(Color.parseColor("#d61313"))
                    viewHolder.itemView.notif_btn.text = "Accept"
                    viewHolder.itemView.notif_btn.setBackgroundColor(Color.parseColor("#1fb529"))
                }else{
                    viewHolder.itemView.notif_reject_btn.isEnabled = false
                    viewHolder.itemView.notif_reject_btn.visibility = View.GONE
                    viewHolder.itemView.notif_btn.isEnabled = false
                }
            }
            when(invitation.status){
                "Pending"->viewHolder.itemView.setBackgroundColor(Color.parseColor("#dbefff"))
                "Accepted"->viewHolder.itemView.setBackgroundColor(Color.parseColor("#dbffdb"))
                "Rejected"->viewHolder.itemView.setBackgroundColor(Color.parseColor("#ffdbdb"))
            }
            Log.d("Logs","$snapshotKey")
            val ref2 = FirebaseDatabase.getInstance().getReference("/invitations/${invitation.clientID}/${currentUser!!.uid}/$snapshotKey")

            viewHolder.itemView.notif_reject_btn.setOnClickListener{
                var temp = invitation
                temp.status = "Rejected"

                ref2.setValue(temp)
                ref.setValue(temp)
                        .addOnSuccessListener {
                            Toast.makeText(viewHolder.itemView.context, "Rejected Invitation",Toast.LENGTH_SHORT).show()
                        }

            }
            viewHolder.itemView.notif_btn.setOnClickListener{
                var temp = invitation
                temp.status = "Accepted"
                ref2.setValue(temp)
                        .addOnSuccessListener {
                            ref.setValue(temp)
                                    .addOnSuccessListener {
                                        Toast.makeText(viewHolder.itemView.context, "Accepted Invitation",Toast.LENGTH_SHORT).show()
                                    }
                        }
            }
        }

        override fun getLayout(): Int {
            return R.layout.notification_tile_client
        }
    }
}