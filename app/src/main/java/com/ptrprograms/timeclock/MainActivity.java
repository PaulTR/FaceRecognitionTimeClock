package com.ptrprograms.timeclock;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.android.things.pio.UartDeviceDriver;

import java.io.IOException;

public class MainActivity extends Activity implements Button.OnButtonEventListener {

    private Button timeClockButton;
    private UartDevice faceDetectorDevice;

    Handler handler = new Handler();
    int delay = 1000; //milliseconds

    private UartDeviceCallback uartDeviceCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            Log.e("Test", "data available");
            return super.onUartDeviceDataAvailable(uart);
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.e("Test", "data error");
            super.onUartDeviceError(uart, error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            timeClockButton = new Button( BoardDefaults.TIME_CLOCK_PIN, Button.LogicState.PRESSED_WHEN_HIGH);
            timeClockButton.setOnButtonEventListener(this);

            PeripheralManagerService service = new PeripheralManagerService();
            faceDetectorDevice = service.openUartDevice("UART0");
            faceDetectorDevice.setBaudrate(9600);
            faceDetectorDevice.setDataSize(8);
            faceDetectorDevice.setParity(UartDevice.PARITY_NONE);
            faceDetectorDevice.setStopBits(1);
            faceDetectorDevice.registerUartDeviceCallback(uartDeviceCallback, new Handler());


        } catch (IOException e) {
            Log.e("Test", "error init button");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if( timeClockButton != null ) {
                timeClockButton.close();
                timeClockButton = null;
            }

            if( faceDetectorDevice != null ) {
                faceDetectorDevice.unregisterUartDeviceCallback(uartDeviceCallback);
                faceDetectorDevice.close();
                faceDetectorDevice = null;
            }

        } catch( IOException e ) {
            Log.e("Test", "error unregistering");
        }
    }

    @Override
    public void onButtonEvent(Button button, boolean down) {
        if( !down ) {
            Log.e("Test", "up");
            String text = "rc01";
            try {
                faceDetectorDevice.write(text.getBytes(), text.length());
            } catch( IOException e ) {
                Log.e("Test", "exception writing uart");
            }
        }
    }
}
