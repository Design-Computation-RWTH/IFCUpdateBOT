package de.rwth_aachen.dc.ifc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.IfcModelInterfaceException;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.utils.IfcUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParseJoC {

	public ParseJoC() throws IOException {
		IfcModelInstance model = new IfcModelInstance();
		// IFC2x3
		IfcModelInterface ifcmodel2x3 = model
				.readModel(Paths.get("./src/main/resources/BIM4Ren_DUNANT_cleaned_IFC2x3.ifc"), Paths.get("."));
		ifcmodel2x3.resetExpressIds();
		ifcmodel2x3.fixOidCounter();

		Path fileName = Path.of("./src/main/resources/CSTB_JoC-example.json");
		String jsonString = Files.readString(fileName);
		execute(ifcmodel2x3, jsonString);
		
		model.saveModel(ifcmodel2x3, Paths.get("c:\\temp\\output15.ifc"));
	}

	private void execute(IfcModelInterface ifcmodel2x3, String jsonString) {
		JSONObject obj = new JSONObject(jsonString);

		JSONArray arr = obj.getJSONArray("changes");
		for (int i = 0; i < arr.length(); i++) {
			String guid;;
			String property_label;;
			String value;
			String description;
			if(arr.getJSONObject(i).has("property_label"))
			{
				// API format
				guid = arr.getJSONObject(i).getJSONObject("object_guid").getString("id");
				property_label = arr.getJSONObject(i).getJSONObject("property_label").getString("value");
				value = arr.getJSONObject(i).getJSONObject("property_value").getString("value");
				description = arr.getJSONObject(i).getJSONObject("property_uri").getString("value");
			}
			else
			{
				// Example file format
				guid = arr.getJSONObject(i).getJSONObject("object").getString("id");
				property_label = arr.getJSONObject(i).getJSONObject("property").getString("label");
				value = arr.getJSONObject(i).getString("value");
				description = arr.getJSONObject(i).getJSONObject("property").getString("uri");
				
			}

			IfcProduct product = (IfcProduct) ifcmodel2x3.getByGuid(guid);
			if (product != null) {
				// System.out.println(product);
				Set<String> properties = IfcUtils.listPropertyNames(product);
				// System.out.println(properties);
				IfcValue ifcValue = IfcModelInstance.getPropertySingleValue(product, property_label);
				if (ifcValue == null)
				{
					try {
						IfcModelInstance.createProperty(product, ifcmodel2x3,property_label,description,value);
					} catch (IfcModelInterfaceException e) {
						e.printStackTrace();
					}
					; 
				}
				else {
					if (ifcValue.getClass().getName().equals("org.bimserver.models.ifc2x3tc1.impl.IfcIdentifierImpl")) {
						System.out.println("String value: " + IfcUtils.nominalValueToString(ifcValue));
						System.out.println(guid + " :" + property_label + " :" + value);
					}
					IfcModelInstance.setStringProperty(ifcmodel2x3, product, property_label, value);
				}
			} else
				System.out.println("GUID does not exist in the model. GUID: " + guid);

		}
	}

	public static void main(String[] args) throws IOException {
		new ParseJoC();
	}

}
