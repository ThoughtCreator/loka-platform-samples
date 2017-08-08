package com.thoughtcreator.iot.apps.console;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.thoughtcreator.iot.api.ApiClient;
import com.thoughtcreator.iot.api.ApiManager;
import com.thoughtcreator.iot.api.exceptions.ConnectionFailedException;
import com.thoughtcreator.iot.api.exceptions.InvalidUsernameOrPasswordException;
import com.thoughtcreator.iot.api.exceptions.UnauthorizedAccessException;
import com.thoughtcreator.iot.api.messages.AnalogMessage;
import com.thoughtcreator.iot.api.messages.ControlMessage;
import com.thoughtcreator.iot.api.messages.DigitalMessage;
import com.thoughtcreator.iot.api.messages.GpioMessage;
import com.thoughtcreator.iot.api.messages.GpsMessage;
import com.thoughtcreator.iot.api.messages.LocationMessage;
import com.thoughtcreator.iot.api.messages.NetworkInformationMessage;
import com.thoughtcreator.iot.api.messages.WifiMessage;
import com.thoughtcreator.iot.api.terminal.Terminal;
import com.thoughtcreator.iot.api.terminal.TerminalEventHandler;

public class Console implements TerminalEventHandler, ApiClient {

	public static Map<Long,Terminal> terminals = new HashMap<Long,Terminal>(1);
	public static ApiManager apiManager = ApiManager.instance;
	public final static Console console = new Console();

	public int received = 0;

	static List<Long> terminalIds = new ArrayList<Long>(1); 
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("Usage: <server> <token> <device_id>[,<device_id 2>[...,<device_id n>]]");
			System.out.println("Example: core.loka.systems hsajk217809-asd109u 123456789,123456788");
			return;
		}


		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down Loka Demo.");
				try {
					Iterator<Entry<Long, Terminal>> it = terminals.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Long,Terminal> e = (Entry<Long,Terminal>) it.next();
						if (e.getValue() != null) {
							apiManager.removeTerminal(e.getValue());						
						}
					}
				} catch (Exception e) { }
			}
		});
 		
		
		String[] terminalSet = args[2].split(Pattern.quote(","));
		for (String terminal : terminalSet) {
			terminalIds.add(Long.parseLong(terminal));			
		}
		
		try {
			apiManager.login("https://" + args[0], args[1]);
		} catch (URISyntaxException e1) {
			
			System.out.println("Error un URI!!!");
			e1.printStackTrace();
			return;
		} catch (UnauthorizedAccessException e1) {
			System.out.println("Unauthorized !!");
			e1.printStackTrace();
			return;
		} catch (ConnectionFailedException e1) {
			System.out.println("Connection failed !!");
			e1.printStackTrace();
			return;
		} catch (InvalidUsernameOrPasswordException e1) {
			System.out.println("Error user failed !!");
			e1.printStackTrace();
			return;
		}


		try {
			for (Long terminalId : terminalIds) {
				Terminal t = new Terminal(terminalId);
				t.setEventHandler(console);
				apiManager.addTerminal(t);
				terminals.put(terminalId, t);
			}
			apiManager.startReceivingEvents(console);
						
			while (true) {
				Thread.sleep(500);
			}

		} catch (Exception e) {
			System.out.println("Exception " + e.toString());
			return;
		}

	}


	@Override
	public void onRegister(Terminal terminal, ControlMessage message) {
		System.out.println("Received Register from " + terminal.getId());
		
	}

	@Override
	public void onGpio(Terminal terminal, GpioMessage message) {
		System.out.println("Received GPIO value from " + terminal.getId() + " in port " + message.getGpio().getPort() + " with value " + message.getGpio().getValue());
	}

	@Override
	public void onDigital(Terminal terminal, DigitalMessage message) {
		System.out.println("Received digital message from " + terminal.getId() + " with value " + message.getDigital().getValue());
	}

	@Override
	public void onAnalog(Terminal terminal, AnalogMessage message) {
		System.out.println("Received analog value from " + terminal.getId() + " in port " + message.getAnalog().getPort() + " with value " + message.getAnalog().getValue());
		
	}

	@Override
	public void onGps(Terminal terminal, GpsMessage message) {
		System.out.println("Received GPS value from " + terminal.getId() + " with latitude " + message.getGps().getLatitude() + " and longitude " + message.getGps().getLongitude());
		
	}

	@Override
	public void onUnknownMessage(Terminal terminal, String message) {
		System.out.println("UNKNOWN MESSAGE ("+ message + ") from terminal " + terminal);
		System.out.println(message);
		
	}

	@Override
	public void onError(Exception ex) {
		System.out.println("ERROR " + ex.getMessage());
		ex.printStackTrace();
		
	}

	public void onUnknownTerminal(long id) {
		System.out.println("UNKNOWN TERMINAL " + id);
		
	}

	public void onWifi(Terminal terminal, WifiMessage message) {
		System.out.println("Received Wifi message: " + message.toString());
	}


	public void onNetworkInformation(Terminal terminal,
			NetworkInformationMessage message) {
		System.out.println("Received NetworkInformation message: " + message.toString());
		
	}

	public void onLocation(Terminal terminal, LocationMessage message) {
		System.out.println("Received Location value from " + terminal.getId() + " with latitude " + message.getLocation().getLatitude() + " and longitude " + message.getLocation().getLongitude());
	}

}
