package Protocols;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;



import java.util.List;

public class ByteToJsonDecoder extends MessageToMessageDecoder<String>{
    private Gson gson;

    ByteToJsonDecoder(){
        gson = new GsonBuilder().create();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list)  {

        try {
            JsonObject obj= gson.fromJson(s,JsonObject.class);
            list.add(obj);
        }
        catch (Exception e){

        }
    }





}
