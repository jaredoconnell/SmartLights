package net.shadowxcraft.smartlights.ui.controllers

import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.shadowxcraft.smartlights.*
import net.shadowxcraft.smartlights.ui.add_led_strip.LEDStripComponentListAdapter

// TODO: Fix memory leaks caused by things referencing each other.

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
class ControllerListAdapter(deviceList: ArrayList<ESP32>, private val clickListener: ClickListener,
    private val pwmButtonClickListener: ButtonClickListener)
    : RecyclerView.Adapter<ControllerListAdapter.ViewHolder?>()
{
    private val devicesList: ArrayList<ESP32> = deviceList

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        var nameTextView: TextView = itemView.findViewById(R.id.controller_name)
        var addPWMDriverButton: Button = itemView.findViewById(R.id.add_pwm_driver_button)
        override fun onClick(v: View?) {
            clickListener.onPositionClicked(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)

        // Inflate the custom layout
        val deviceView: View = inflater.inflate(R.layout.item_controller, parent, false)

        // Return a new holder instance
        return ViewHolder(deviceView)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the data model based on position
        val device: ESP32 = devicesList[position]

        // Set item views based on your views and data model
        val nameTextView: TextView = holder.nameTextView
        nameTextView.text = device.name
        val button: Button = holder.addPWMDriverButton
        button.setOnClickListener {
            pwmButtonClickListener.onButtonClicked(position)
        }
    }
}