package moonfather.mahjong;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;


@Mod(EndermenMahjong.MODID)
public class EndermenMahjong
{
    public static final String MODID = "endermanmahjong";
    private static final Logger LOGGER = LogUtils.getLogger();


    public EndermenMahjong(IEventBus modEventBus, ModContainer modContainer)
    {
        // nothing here.   we're going to do this fabric-style.  and i'll port to fabric next week.
    }
}
