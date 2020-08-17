package net.floodlightcontroller.resmon;
 
import java.util.Collection;
import java.util.Map;
 
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.MacAddress;
 
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;


import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemTracker implements IOFMessageListener, IFloodlightModule {
 
    protected IFloodlightProviderService floodlightProvider;
    protected static Logger logger;

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return MemTracker.class.getSimpleName();
    }
 
    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }
 
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }
 
    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
           floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
            //macAddresses = new ConcurrentSkipListSet<Long>();
            logger = LoggerFactory.getLogger(MemTracker.class);
	    logger.info("Mem Track loaded");
 
    }
 
    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
 
    }
 
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        
                try {
        Ethernet eth =
                IFloodlightProviderService.bcStore.get(cntx,
                                            IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
        //logger.info("MAC Address: {} seen on switch: {}",
        //            eth.getSourceMACAddress().toString(),
        //           sw.getId().toString());
  
         switch (msg.getType()) {
            case PACKET_IN:

                if (eth.getEtherType() == EthType.IPv4) {
                    IPv4 ip = (IPv4) eth.getPayload();
                    IPv4Address srcIp = ip.getSourceAddress();
                    IPv4Address dstIp = ip.getDestinationAddress();
                    
                    logger.info("PACKET-IN IPv4 src IP " + ((srcIp != null) ? srcIp.toString() : "")
                        + ", dst IP " + ((dstIp != null) ? dstIp.toString() : "")
                        + ", fragment " + (ip.isFragment() ? ip.getFragmentOffset() : 0)
                        + ", len " + ip.getTotalLength());
                
                    //if (ip.getProtocol().equals(IpProtocol.TCP)) {
                    //    TCP tcp = (TCP) ip.getPayload();
                     //   logger.info("TCP: src port" + (short)tcp.getSourcePort().getPort()
                      //      + ", dst port " + (short)tcp.getDestinationPort().getPort()
                       //     + ", seq " + tcp.getSequence()
                        //    + ", ack " + tcp.getAcknowledge());

                   // } else if (ip.getProtocol().equals(IpProtocol.UDP)) {
                //        UDP udp = (UDP) ip.getPayload();
               // 
                 //       logger.info("UDP: src port" + (short)udp.getSourcePort().getPort()
                  //          + ", dst port " + (short)udp.getDestinationPort().getPort()
                   //         + ", len " + udp.getLength());
                    //}
                } else if (eth.getEtherType() == EthType.ARP) { /* shallow check for equality is okay for EthType */
                    logger.info("#FC### ARP packet received.");
                } else if (eth.getEtherType() == EthType.IPv6) {
                    IPv6 ip = (IPv6) eth.getPayload();
                    IPv6Address srcIp = ip.getSourceAddress();
                    IPv6Address dstIp = ip.getDestinationAddress();
                    
                    logger.info("PACKET-IN IPv6 src IP " + ((srcIp != null) ? srcIp.toString() : "null")
                        + ", dst IP " + ((dstIp != null) ? dstIp.toString() : "null"));

                    if (ip.getNextHeader().equals(IpProtocol.TCP)) {
                        TCP tcp = (TCP) ip.getPayload();
                        logger.info("TCP: src port" + (short)tcp.getSourcePort().getPort()
                            + ", dst port " + (short)tcp.getDestinationPort().getPort()
                            + ", seq " + tcp.getSequence()
                            + ", ack " + tcp.getAcknowledge());

                    } else if (ip.getNextHeader().equals(IpProtocol.UDP)) {
                        UDP udp = (UDP) ip.getPayload();
                        logger.info("UDP: src port" + (short)udp.getSourcePort().getPort()
                            + ", dst port " + (short)udp.getDestinationPort().getPort()
                            + ", len " + udp.getLength());
                    }
                }
                break;
            case FLOW_REMOVED:
                logger.info("#FC### FLOW_REMOVED ");
                try {
                    OFFlowRemoved flowRemoved = (OFFlowRemoved) msg;
                    IPv4Address ip = flowRemoved.getMatch().get(MatchField.IPV4_SRC);
                    logger.info("#FC### FLOW_REMOVED - Match IP SRC {};", ip.toString());
                } catch (Exception e) {
                    logger.info("$$$$$$$$$$$");
                }
                break;
            default:
                logger.info("#FC### unknown packet type {}", msg.getType());
                break;
        }
        } catch (Exception e) {
             logger.info("$$$$$$$$$$$");
        }

        int kb = 1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        logger.info("Memory Used/Free/Total/MAX :" 
			+ (runtime.totalMemory() - runtime.freeMemory()) / kb + " KB/ " + runtime.freeMemory() / kb + " KB/"
        		+ runtime.totalMemory() / kb + " KB/ " + runtime.maxMemory() / kb + " KB"); 
        return Command.CONTINUE;
    }
 
}
