package net.shadowxcraft.smartlights.ui.add_led_strip

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.util.keyIterator
import androidx.core.util.valueIterator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.shadowxcraft.smartlights.*
import net.shadowxcraft.smartlights.R.layout


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LEDStripComponentFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LEDStripComponentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LEDStripComponentFragment(private val controller: ESP32) : Fragment() {
    //private var param1: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var adapter: LEDStripComponentListAdapter? = null

    private var components: ArrayList<LEDStripComponent> = ArrayList()
    private var colors: HashMap<String, Color> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
        }
        colors["Red"] = Color(255, 0, 0)
        colors["Green"] = Color(0, 255, 0)
        colors["Blue"] = Color(0, 0, 255)
        colors["3200k"] = Color(3200, 4095)
        colors["4000k"] = Color(4000, 4095)
        colors["5000k"] = Color(5000, 4095)
        colors["6500k"] = Color(6500, 4095)
    }

    private fun pinAlreadyInUse(pin: Int, driver: PWMDriver) : Boolean {
        for (component in components) {
            if (component.driverPin == pin)
                return true
        }
        for (strip in controller.ledStrips.valueIterator()) {
             for (component in strip.components) {
                 if (component.driver == driver && component.driverPin == pin)
                     return true
             }
        }
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val currentView: View = inflater.inflate(layout.fragment_led_strip_components, container, false)
        val addButton: View = currentView.findViewById(R.id.add_led_strip_component_floating_action_button)
        addButton.setOnClickListener {
            val builder = AlertDialog.Builder(this.activity)
            val view = inflater.inflate(layout.new_led_strip_component, null)
            val pinSelector: NumberPicker = view.findViewById(R.id.led_strip_pin_selector)
            val colorSelector: Spinner = view.findViewById(R.id.color_picker_spinner)
            val driverSelector: Spinner = view.findViewById(R.id.driver_picker_spinner)
            pinSelector.minValue = 0
            pinSelector.maxValue = 15

            val colorValues = arrayOf(
                "Red", "Green", "Blue", "3200k", "4000k", "5000k", "6500k"
            )
            val colorAdapter: ArrayAdapter<String> = ArrayAdapter(
                this.requireContext(),
                android.R.layout.simple_spinner_item,
                colorValues
            )
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            colorSelector.adapter = colorAdapter

            val driverValues = Array(controller.pwmDrivers.size()) { "" }
            var i = 0;
            for (keyAddr in controller.pwmDrivers.keyIterator()) {
                driverValues[i++] = keyAddr.toString();
            }

            val driverAdapter: ArrayAdapter<String> = ArrayAdapter(
                this.requireContext(),
                android.R.layout.simple_spinner_item,
                driverValues
            )
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            driverSelector.adapter = driverAdapter


            val dialog = builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.finish_adding_led_strip_component, null)
                .create()
            // Override click listener to prevent it from closing when invalid.
            dialog.setOnShowListener {
                val button: Button =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    // First, validate the input
                    val pin = pinSelector.value
                    val driverID = (driverSelector.selectedItem as String).toInt()
                    val driver = controller.pwmDrivers[driverID]
                    val color = colors[colorSelector.selectedItem]

                    if (pinAlreadyInUse(pin, driver)) {
                        Toast.makeText(
                            BLEControllerManager.activity,
                            "Port already in use.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        components.add(LEDStripComponent(color!!, driver, pin))
                        adapter?.notifyDataSetChanged()

                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
        }
        val completeButton: View = currentView.findViewById(R.id.complete_led_strip_components)
        completeButton.setOnClickListener {
            val nameComponent: EditText = currentView.findViewById(R.id.new_led_strip_name)
            val name = nameComponent.text.toString()
            when {
                components.isEmpty() -> {
                    Toast.makeText(
                        BLEControllerManager.activity,
                        "Please add components.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                name.isEmpty() -> {
                    Toast.makeText(
                        BLEControllerManager.activity,
                        "Please give the LED strip a name.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val nextID = controller.getNextLEDStripID()
                    val newStrip = LEDStrip(nextID, name, components, null, controller)
                    controller.addLEDStrip(newStrip)
                    (context as MainActivity).supportFragmentManager.popBackStack()
                }
            }
        }

        // Create adapter passing in the led strip components
        adapter = LEDStripComponentListAdapter(components)

        // Lookup the recyclerview in activity layout
        val rvControllers = currentView.findViewById(R.id.list_controllers) as RecyclerView
        rvControllers.setHasFixedSize(true)
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvControllers.addItemDecoration(itemDecoration)


        // Attach the adapter to the recyclerview to populate items
        rvControllers.adapter = adapter

        // Set layout manager to position the items
        rvControllers.layoutManager = LinearLayoutManager(context)

        return currentView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}