import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import thrift.Operation;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import thrift.Vertex;

/**
 *
 * @author Eduardo Vieira e Sousa
 */

public class OperationServer{
  
    public static OperationHandler handler;
    public static Operation.Processor processor;

    public static void main(String [] args) {
        try{
            List<String[]> configs = new ArrayList<>();
            Scanner scanner = new Scanner(new FileReader("C:\\Users\\Livionildo\\Documents\\NetBeansProjects\\EduardoSD\\src\\ServerConfig.txt")).useDelimiter("\\n");
            while (scanner.hasNext()) {
                String linha = scanner.next();
                String[] conf = linha.split(";");
                configs.add(conf);
            }
            
            //int arg = Integer.parseInt(args[0]); // Qual ele é na lista
            int arg = 4;
            
            handler = new OperationHandler();
            processor = new Operation.Processor(handler);           

            // Seta configurações do arquivo         
            handler.setId(Integer.parseInt(configs.get(arg)[0]));
            handler.setPort(Integer.parseInt(configs.get(arg)[1]));
            handler.setM(Integer.parseInt(configs.get(arg)[2]));
            handler.buildTable(configs);
            
            Runnable serverThread = new Runnable(){
		public void run(){
                    poolServer(processor);
		}
            };  
                        
            new Thread(serverThread).start();
                             
        }catch(Exception x){
             x.printStackTrace();
        }
    }
    
    public static void poolServer(Operation.Processor processor) {
        try{
            TServerTransport serverTransport = new TServerSocket(handler.getPort()+handler.getId());         
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
                       
            System.out.println("Starting the multithread server: ID["+handler.getId()+"] || " + "PORT["+(handler.getPort()+handler.getId())+"]");
            server.serve();
                
        }catch(Exception e) {
          e.printStackTrace();
        }
    }    
}
