package ak.ak32767.projecte.utils;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.util.Base64;
import java.util.List;

public class ItemBase64Converter {
    private ItemBase64Converter() {}

    public static String item2Base64(List<ItemStack> items) {
        try (FastByteArrayOutputStream bytesStream = new FastByteArrayOutputStream(1024);
             BukkitObjectOutputStream data = new BukkitObjectOutputStream(bytesStream)) {

            data.writeInt(items.size());
            for (ItemStack item : items)
                data.writeObject(item);

            data.flush();
            return Base64.getEncoder().encodeToString(bytesStream.array);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<ItemStack> base642Item(String b64) {
        if (b64 == null || b64.isEmpty())
            return new ObjectArrayList<>();

        try (FastByteArrayInputStream byteStream = new FastByteArrayInputStream(Base64.getDecoder().decode(b64));
             BukkitObjectInputStream data = new BukkitObjectInputStream(byteStream)) {

            int length = data.readInt();
//            ItemStack[] items = new ItemStack[length];
            List<ItemStack> items = new ObjectArrayList<>();

            for (int i = 0; i < length; i++)
                items.add((ItemStack) data.readObject());

            return items;
        } catch (Exception e) {
            return new ObjectArrayList<>();
        }
    }

}
