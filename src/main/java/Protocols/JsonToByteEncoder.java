package Protocols;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;


public class JsonToByteEncoder extends MessageToMessageEncoder<Object>{
   private Gson gson;
   JsonToByteEncoder(){
       gson = new GsonBuilder().create();
   }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, List<Object> list){
       try {
           String s= gson.toJson(obj);
           list.add(s+"\n");
       }
       catch (Exception e){
            e.printStackTrace();
       }

    }


}
