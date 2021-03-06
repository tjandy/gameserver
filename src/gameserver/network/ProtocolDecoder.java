package gameserver.network;

import gameserver.network.core.BaseClientPacket;
import gameserver.network.core.PacketManager;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 接受的二进制流不能有多余的数据（很蹩脚）
 *
 * @author caoxin
 */
public class ProtocolDecoder implements MessageDecoder {

    private Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    /**
     * 判断接受到二进制流,是否可以进行解析
     *
     * @param session
     * @param in
     * @return
     */
    @Override
    public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
        int remaining = in.remaining();
        if (remaining < 6) {
            return MessageDecoderResult.NOT_OK;
        }
        int opcode = in.getShort();
        int len = in.getInt();
        int lastRemaining = in.remaining();
        System.out.println(remaining + "\t" + opcode + "\t" + len + "\t" + lastRemaining);
        if (lastRemaining < len) {
            return MessageDecoderResult.NOT_OK;
        }
        return MessageDecoderResult.OK;
    }

    /**
     * 解析接受客户端的二进制流
     *
     * @param ioSession
     * @param in
     * @param out
     * @return
     * @throws Exception
     */
    @Override
    public MessageDecoderResult decode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        short opcode = in.getShort();
        in.getInt();
        BaseClientPacket clientPacket = PacketManager.getPacketByOpcode(opcode);
        if (clientPacket == null) {
            return MessageDecoderResult.NOT_OK;
        }
        clientPacket.read(in.buf());
        clientPacket.setIoSession(ioSession);
        out.write(clientPacket);
        return MessageDecoderResult.OK;
    }

    /**
     * 完成解析工作
     *
     * @param session
     * @param out
     * @throws Exception
     */
    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
    }
}