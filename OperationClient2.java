import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import thrift.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/**
 *
 * @author Eduardo Vieira e Sousa
 */

public class OperationClient2{
    public static void main(String [] args){
        try{
            //String arg1 = args[0]; // "localhost"
            //int arg2 = Integer.parseInt(args[1]); // 9090


            TTransport transport = new TSocket("localhost",9106);
            TProtocol protocol = new  TBinaryProtocol(transport);
            Operation.Client client = new Operation.Client(protocol);
            transport.open();

            //=========================================================
            // Teste: Teste de concorrência
            // ( Reiniciar o servidor e usar os serviços de teste )
            // ( Iniciar os dois clientes simultaneamente )
            //=========================================================
            
            //client.testServWait();
            //client.testServAddV(12);
            //client.testServRmvV(1);
            //client.testServClearV();
            
            try{
               
                //Ler vértice
                client.addVrt(1,1,1.1,"vrt1");
                client.addVrt(2,2,2.2,"vrt2");
                client.addVrt(3,3,3.3,"vrt3");           
                client.addVrt(4,4,4.4,"vrt4");
                client.addVrt(5,5,5.5,"vrt5");
                client.addVrt(6,6,6.6,"vrt6");
                client.addEdg(1,1,4,7.0,"edg1",false);
                client.addEdg(2,1,3,1.0,"edg2",false);
                client.addEdg(3,1,2,1.0,"edg3",false);
                client.addEdg(4,4,3,5.0,"edg4",false);
                client.addEdg(5,3,5,2.0,"edg5",false);
                client.addEdg(6,3,2,1.0,"edg6",false);
                client.addEdg(7,5,2,7.0,"edg7",false);
                client.addEdg(8,3,6,4.0,"edg8",false);
                client.addEdg(9,2,6,3.0,"edg9",false);
                client.addEdg(10,3,2,3.0,"edg9",false);
   
                //client.delVrt(4);
                //client.delEdg(8);
                
                //Vertex v = client.readVrt(5);
                //Edge e = client.readEdg(3);
            
                List<Vertex> lv;                     
                List<Edge> le;
                
                //System.out.println(v.toString());
                //System.out.println(e.toString());
                  
                lv = client.listAllVrts();
                le = client.listAllEdgs();
                
                for(int i = 0; i < le.size(); i++)
                System.out.println(le.get(i).toString());
        
                for(int i = 0; i < lv.size(); i++)
                System.out.println(lv.get(i).toString());
          
   
                le = client.listVrtEdgs(5);
                lv = client.listVrtNgbs(5);
                
                for(int i = 0; i < le.size(); i++)
                System.out.println(le.get(i).toString());
        
                for(int i = 0; i < lv.size(); i++)
                System.out.println(lv.get(i).toString());
                
               
                le = client.shrtPath(1, 6);
                
                for(int i = 0; i < le.size(); i++)
                System.out.println(le.get(i).toString());
                                    
                
            }catch(InvalidOperation e){              
                System.out.println("["+e.hora+"] "+e.msg);
            }
             
            transport.close(); 
        }catch(TException x){
            x.printStackTrace();
        }
    }
}
