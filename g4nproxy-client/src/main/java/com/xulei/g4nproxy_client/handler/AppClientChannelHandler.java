package com.xulei.g4nproxy_client.handler;


import com.xulei.g4nproxy_client.Constants;
import com.xulei.g4nproxy_client.util.LogUtil;
import com.xulei.g4nproxy_protocol.protocol.ProxyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lei.X
 * @date 2019/3/18 10:12 AM
 */

@Slf4j
public class AppClientChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static final String tag = "appClient_tag";



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) throws Exception {

        LogUtil.i(tag,"receive message: "+msg.toString());
        switch (msg.getType()){
            case ProxyMessage.C_TYPE_AUTH:
                handleAuthMessage(ctx,msg);
                break;
//            case ProxyMessage.TYPE_CONNECT:
//                handleConnectMessages(ctx,msg);
//                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx, msg);
                break;
        }



        //将这个管道放到Map中，方便服务器返回数据时调用
        Constants.manageCtxMap.put(Constants.DATA_CHANNEL,ctx);

//        ctx.fireChannelRead(msg);


    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.i(tag,"app端的通道被激活");
        super.channelActive(ctx);
    }

    private void handleAuthMessage(ChannelHandlerContext ctx,ProxyMessage message){


        LogUtil.i(tag,"请求服务器的认证请求： "+new String(message.getData()));
    }


    /**
     * TODO 真正的代理实现
     * 处理传输数据的请求
     * @param ctx
     * @param msg
     */
    private void handleTransferMessage(ChannelHandlerContext ctx,ProxyMessage msg){
        // 测试返回是否成功
        LogUtil.w(tag,"代理服务器处理传输数据的请求");

        Channel littleProxyChannel = Constants.manageChannelMap.get(Constants.LOCAL_SERVER_CHANNEL);

        byte[] data  = msg.getData();

//        LogUtil.i("TEST",new String(data));

        ByteBuf encoded = ctx.alloc().buffer(4 * data.length);
        encoded.writeBytes(data);

        littleProxyChannel.writeAndFlush(encoded);

    }


    /**
     * 处理连接请求
     * TODO 废弃的请求
     * @param ctx
     * @param msg
     */
    private void handleConnectMessages(ChannelHandlerContext ctx, ProxyMessage msg){

        LogUtil.i(tag,"进入连接处理模块");

        //获取数据传输的channel
        Channel dataChannel = ctx.channel().attr(com.xulei.g4nproxy_protocol.protocol.Constants.NEXT_CHANNEL).get();
        ProxyMessage testMesage = new ProxyMessage();
        testMesage.setData(testData());
        testMesage.setUri("test");
        dataChannel.writeAndFlush(testMesage);

        //测试数据发回的位置
        testMesage.setUri("testCTX");
        ctx.channel().writeAndFlush(testMesage);

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        LogUtil.e(tag,"appClientHandler error",cause);
        // 当出现异常就关闭连接
        ctx.close();
    }


    /**
     * 和 目标主机 建立连接
     */
//    private ChannelFuture connectAppBootStrap(ChannelHandlerContext ctx, Object msg){
//
//        //建立一个BootStrap
//        BootStrapFactory bootStrapFactory = new BootStrapFactory();
//        Bootstrap bootstrap = bootStrapFactory.build();
//
//        return bootstrap.handler(new AppClientChannelHandler(ctx))
//
//
//
//        if (isHttp){
//            return bootstrap.handler(new HttpConnectChannelInitializer(ctx))
//                    .connect(address)
//                    .addListener(new HttpChannelFutureListener(msg,ctx));
//        }
//        //如果为https请求
//        return bootstrap.handler(new HttpsConnectChannelInitializer(ctx))
//                .connect(address)
//                .addListener(new HttpsChannelFutureListener(msg,ctx));
//    }


    private byte[] testData(){
        ByteBuf buf = Unpooled.buffer(10);
        byte[] bytes = {1,2,3,4,5};
        buf.writeBytes(bytes);

//        return buf;
        return bytes;
    }

}
