import java.util.Arrays;
import java.lang.String;

/**
 * This is an RCON client class
 * @author josep
 *
 */
public class CommandRefs {
	// Attributes
	private static final String HOTBAR = "hotbar";
	private static final String INVENTORY = "inventory";
	private static final int INV_SLOT_OFFSET	= 9;
	private static final int HOTBAR_SIZE 		= 8;
	public CommandRefs() {
		//Not really anything I would do with the abstract class tbh
	}
	
	public static String generateGiveCoordBook(String pagesText, String target) {
		String cmd = "give "+target+" written_book{pages:['[["
					+ "\"COORDINATES!\\\\n\","
					+ "{\"text\":\"Hover for all coords\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""+pagesText+"\"}}"
					+ "]]'],title:\"Player Coords\",author:\"Omniscient Server\",display:{Name:'[{\"text\":\"Player Data\",\"color\":\"#cc00ff\",\"bold\":true,\"italic\":false}]',Lore:['[{\"text\":\""+target+"\",\"italic\":false}]']},Owner:"+target+"}";
		// Add pages for future commands that filter the coordinates
		return cmd;
	}
	
	public static String generateModifyCoordBook(String pagesText, String target, String slot) {
		// Determine whether we are replacing a book in the inventory or in the hotbar
		String hbOrInv = Integer.parseInt(slot) < HOTBAR_SIZE ? HOTBAR : INVENTORY;
		// Normalize the slot number for hotbar and inventory
		slot = Integer.parseInt(slot) < HOTBAR_SIZE ? slot : Integer.toString(Integer.parseInt(slot) - INV_SLOT_OFFSET);
		String cmd = "item replace entity "+target+" "+hbOrInv+"."+slot+" with written_book{pages:['[["
					+ "\"COORDINATES!\\\\n\","
					+ "{\"text\":\"Hover for all coords\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\""+pagesText+"\"}}"
					+ "]]'],title:\"Player Coords\",author:\"Omniscient Server\",display:{Name:'[{\"text\":\"Player Data\",\"color\":\"#cc00ff\",\"bold\":true,\"italic\":false}]',Lore:['[{\"text\":\""+target+"\",\"italic\":false}]']},Owner:"+target+"}";
		// Add pages for future commands that filter the coordinates
		return cmd;
	}
	
	public static String generateTellRawMsg(String msg, String target) {
		String cmd = "tellraw "+target+" {\"text\":\""+msg+"\"}";
		// Can add a parameter to designate special text and stuff like that?
		return cmd;
	}
	
	public static String generateGetItemSlot(String itemData, String target) {
		String cmd = "data get entity "+target+" Inventory["+itemData+"].Slot";
		return cmd;
	}
}