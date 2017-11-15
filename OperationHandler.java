import static java.lang.Math.pow;
import thrift.*;
import org.apache.thrift.TException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import java.math.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author Eduardo Vieira e Sousa
 */

public class OperationHandler implements Operation.Iface{
    
    private Graph g = new Graph();
    private ConcurrentSkipListSet<Integer> emUsoVrt = new ConcurrentSkipListSet<Integer>();
    private ConcurrentSkipListSet<Integer> emUsoEdg = new ConcurrentSkipListSet<Integer>();
    
    private int id;
    private int m;
    private int port;
    private int[] fingerTable;
    
    private TTransport transport;
    private TProtocol protocol;
    private Operation.Client client;
        
    public void setId(int id){
        this.id = id;
    }
    
    public void setM(int m){
        this.m = m;
    }
    
    public void setPort(int x){
        this.port = x;
    }
    
     public int getId(){
        return this.id;
    }
    
    public int getM(){
        return this.m;
    }
    
    public int getPort(){
        return this.port;
    }
   
    public int hash(int key){       
        return (int)(key%(pow(2,m)));
    }
        
    @Override  // OK!
    public int checkTable(int k) throws InvalidOperation, TException{
        int key = hash(k);
        int server;
        
        if((id==key) || (id==fingerTable[0])){
            return id;
        }
                
        if(((id<key) && (fingerTable[0]>key))){
            return fingerTable[0];
        }
        
        if(((id>fingerTable[0]) && (key>id)) || ((id>fingerTable[0]) && (key<fingerTable[0]))){
            return fingerTable[0];
        }
              
        for(int i = 0; i<this.fingerTable.length; i++){
            if(key >= fingerTable[i]){
                server = fingerTable[i];
                this.transport = new TSocket("localhost",9090+server);
                this.protocol = new  TBinaryProtocol(transport);
                this.client = new Operation.Client(protocol);
                transport.open();
                server = client.checkTable(k);
                transport.close();
                return  server;
            }
        }
      
        server = fingerTable[0];
        this.transport = new TSocket("localhost",9090+server);
        this.protocol = new  TBinaryProtocol(transport);
        this.client = new Operation.Client(protocol);
        transport.open();
        server = client.checkTable(k);
        transport.close();
        return  server;
    }
    
    // OK!
    private int succ(List<String[]> lista, int x){
        List<Integer> sucMaior = new ArrayList<>(); 
        List<Integer> sucMenor = new ArrayList<>();

        int res;
        for(int j= 0; j<lista.size(); j++){
            if(Integer.parseInt(lista.get(j)[0]) >= x){
                sucMaior.add(Integer.parseInt(lista.get(j)[0]));
            }else{
                sucMenor.add(Integer.parseInt(lista.get(j)[0]));
            }      
        }      
        if(sucMaior.isEmpty()){
            res = sucMenor.get(0);
            for(int j= 0; j<sucMenor.size(); j++){
                if(res > sucMenor.get(j))
                    res = sucMenor.get(j);
            }
        }else{
            res = sucMaior.get(0);
            for(int j= 0; j<sucMaior.size(); j++){
                if(res > sucMaior.get(j))
                    res = sucMaior.get(j);
            }
        }
        
        return res;
    }
    
    // OK!
    public void buildTable(List<String[]> lista){
        fingerTable = new int[m];  
        for(int i= 0; i<m;i++){
            fingerTable[i]= succ(lista,(int)(id+pow(2,i))%(int)(pow(2,m)));
        }
        System.out.println("\tFingerTable:");
        for(int i= 0; i<m;i++){
        System.out.println("\t|"+i+"\t|"+fingerTable[i]+"\t|");
        }
    }
           
    // OK!
    private Boolean checaVrt(int nome){ // Verifica já existe um vértice com o nome em servidor; 
        while(!emUsoVrt.add(nome));          
        // Região crítica
        boolean r = false;
        for(int i = g.getVertSize(); i>0; i--)
            if(g.getVert().get(i-1).getNome() == nome)
                r = true;
        // Região crítica
        emUsoVrt.remove(nome);

        return r;       
    }

    // OK!
    private int getIndexVrt(int nome){ // Retorna o índice do vértice;
        int r = -1;
        for(int i = g.getVertSize(); i>0; i--)
            if(g.getVert().get(i-1).getNome() == nome)
                r = i-1;
      
        return r;
    }
    
    // OK!
    private Boolean checaEdg(int nome){ // Verifica já existe um aresta com o nome;
        while(!emUsoEdg.add(nome));
        // Região crítica
        boolean r = false;
        for(int i = g.getArestSize(); i>0; i--)
            if((g.getArest().get(i-1).getNome() == nome))
                r = true;
        // Região crítica
        emUsoEdg.remove(nome);
        
        return r;
    }
    
    // OK!
    private int getIndexEdg(int nome){ // Retorna o índice da aresta;
        int r = -1;
        for(int i = g.getArestSize(); i>0; i--)
            if((g.getArest().get(i-1).getNome() == nome))
                r = i-1;
        
        return r;
    }
    
    @Override // OK!
    public boolean checkVrt(int nome){
        while(!emUsoVrt.add(nome));          
        // Região crítica
        boolean r = false;
        for(int i = g.getVertSize(); i>0; i--)
            if(g.getVert().get(i-1).getNome() == nome)
                r = true;
        // Região crítica
        emUsoVrt.remove(nome);

        return r;       
    }
    
    @Override // OK!
    public void addAdj(int v, Edge nome) throws InvalidOperation, TException{        
        if(checaVrt(v)){       
            while(!emUsoVrt.add(v));
            // Região crítica
            g.getVert().get(getIndexVrt(v)).addToAdja(nome); 
            // Região crítica
            emUsoVrt.remove(v);
        }else{           
            InvalidOperation io = new InvalidOperation();
            LocalDateTime now = LocalDateTime.now();
            io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
            io.msg = "O grafo não possui um vértice com o nome: "+v;
            throw io;
        }
    }
    
    @Override // OK!
    public void delAdj(int v, Edge nome) throws InvalidOperation, TException{
        if(checaVrt(v)){
            while(!emUsoVrt.add(v));
            // Região crítica
            g.getVert().get(getIndexVrt(v)).getAdja().remove(nome);
            // Região crítica
            emUsoVrt.remove(v);
        }else{           
            InvalidOperation io = new InvalidOperation();
            LocalDateTime now = LocalDateTime.now();
            io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
            io.msg = "O grafo não possui um vértice com o nome: "+nome;
            throw io;
        }
    }
        
    @Override // OK!
    public void addVrt(int nome, int cor, double peso, String desc) throws InvalidOperation, TException{     
        int server = checkTable(nome);
        System.out.println("SERVER: "+server);
     
        if(server == this.id){      
        
            if(checaVrt(nome)){            
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo já possui um vértice com o nome: "+nome;
                throw io;
            }else{          
                while(!emUsoVrt.add(nome));          
                // Região crítica            
                Vertex novo = new Vertex(nome,cor,peso,desc,null);
                g.addToVert(novo);   
                // Região crítica
                System.out.println("addVrt("+nome+ ", "+cor+", "+peso+", "+desc+")");
                emUsoVrt.remove(nome);     
            } 
        }else{
                this.transport = new TSocket("localhost",9090+server);
                this.protocol = new  TBinaryProtocol(transport);
                this.client = new Operation.Client(protocol);
                transport.open();
                client.addVrt(nome, cor, peso, desc);
                transport.close();                       
        }
        
    }

    @Override // OK!
    public void delVrt(int nome) throws InvalidOperation, TException{  
        int server = checkTable(nome);
                    
        if(server == this.id){   
            if(checaVrt(nome)){
                Vertex temp = g.getVert().get(getIndexVrt(nome)); 
                for(int i = temp.getAdjaSize(); i > 0 ; i--)
                    this.delEdg(temp.getAdja().get(i-1).getNome());
                while(!emUsoVrt.add(nome));
                // Região crítica         
                g.getVert().remove(getIndexVrt(nome));    
                // Região crítica
                System.out.println("delVrt("+nome+");"); 
                emUsoVrt.remove(nome);
            }else{           
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui um vértice com o nome: "+nome;
                throw io;
            }
        }else{
                this.transport = new TSocket("localhost",9090+server);
                this.protocol = new  TBinaryProtocol(transport);
                this.client = new Operation.Client(protocol);
                transport.open();
                client.delVrt(nome);
                transport.close();                       
        }           
    }

    @Override // OK!
    public void updtVrt(int nome, int cor, double peso, String desc) throws InvalidOperation, TException{           
        int server = checkTable(nome);
                    
        if(server == this.id){  
            if(checaVrt(nome)){
                while(!emUsoVrt.add(nome));                
                // Região crítica         
                g.getVert().get(getIndexVrt(nome)).setCor(cor);
                g.getVert().get(getIndexVrt(nome)).setPeso(peso);
                g.getVert().get(getIndexVrt(nome)).setDesc(desc);
                // Região crítica
                System.out.println("updtVrt("+nome+ ", "+cor+", "+peso+", "+desc+")");
                emUsoVrt.remove(nome);
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui um vértice com o nome: "+nome;
                throw io;
            }
        }else{
                this.transport = new TSocket("localhost",9090+server);
                this.protocol = new  TBinaryProtocol(transport);
                this.client = new Operation.Client(protocol);
                transport.open();
                client.updtVrt(nome, cor, peso, desc);
                transport.close();                       
        }
    }

    @Override // OK!
    public Vertex readVrt(int nome) throws InvalidOperation, TException{
        int server = checkTable(nome);
        Vertex novo;
                    
        if(server == this.id){  
            if(checaVrt(nome)){       
                while(!emUsoVrt.add(nome));             
                // Região crítica     
                novo = g.getVert().get(getIndexVrt(nome));
                // Região crítica
                System.out.println("readVrt("+nome+")");
                emUsoVrt.remove(nome);

                return novo;   
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui um vértice com o nome: "+nome;
                throw io;
            }
        }else{
                this.transport = new TSocket("localhost",9090+server);
                this.protocol = new  TBinaryProtocol(transport);
                this.client = new Operation.Client(protocol);
                transport.open();
                novo = client.readVrt(nome);
                transport.close();
                
                return novo;
        } 
    }

    @Override // OK!
    public void addEdg(int nome, int v1, int v2, double peso, String desc, boolean dir) throws InvalidOperation, TException{ 
        
        int server = checkTable(nome);
        int serverV1 = checkTable(v1);
        int serverV2 = checkTable(v2);
        
        boolean flagV1;
        boolean flagV2;
        
        if(serverV1 == this.id){       
            if(!checaVrt(v1)){
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui vértice com com o nome: "+v1;
                throw io;
            }
        }else{  
            this.transport = new TSocket("localhost",9090+serverV1);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            flagV1 = client.checkVrt(v1);
            transport.close();

            if(!flagV1){             
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui vértice com com o nome: "+v1;
                throw io;
            }
        }
        
        if(serverV2 == this.id){ 
            if(!checaVrt(v2)){
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui vértice com com o nome: "+v2;
                throw io;
            }
        }else{
            this.transport = new TSocket("localhost",9090+serverV2);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            flagV2 = client.checkVrt(v2);          
            transport.close();          
            
            if(!flagV2){         
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui vértice com com o nome: "+v2;
                throw io;
            }
        }
                    
        if(server == this.id){ 
            if(checaEdg(nome)){             
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo já possui uma aresta com o nome: "+nome;
                throw io;
            }else{ 
                while(!emUsoEdg.add(nome));
                while(!emUsoVrt.add(v1));
                if(v1 != v2)
                    while(!emUsoVrt.add(v2));
 
                // Região crítica
                Edge novo = new Edge(nome, v1, v2, peso, desc, dir);
                g.addToArest(novo);
                // Região crítica
                emUsoEdg.remove(nome);                                
                if(serverV1 == this.id){
                    g.getVert().get(getIndexVrt(v1)).addToAdja(novo);
                    emUsoVrt.remove(v1);
                }else{
                    emUsoVrt.remove(v1);
                    this.transport = new TSocket("localhost",9090+serverV1);
                    this.protocol = new  TBinaryProtocol(transport);
                    this.client = new Operation.Client(protocol); 
                    transport.open();
                    client.addAdj(v1, novo);
                    transport.close();
                } 
                        
                if(serverV2 == this.id){
                    g.getVert().get(getIndexVrt(v2)).addToAdja(novo);
                    emUsoVrt.remove(v2);
                }else{
                    emUsoVrt.remove(v2);
                    this.transport = new TSocket("localhost",9090+serverV2);
                    this.protocol = new  TBinaryProtocol(transport);
                    this.client = new Operation.Client(protocol);      
                    transport.open();
                    client.addAdj(v2, novo);
                    transport.close();
                }
                
                System.out.println("addEdg("+nome+", "+v1+ ", "+v2+", "+peso+", "+desc+", "+dir+")");
            }
        }else{
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            client.addEdg(nome, v1, v2, peso, desc, dir);
            transport.close();  
        }
    }

    @Override // OK
    public void delEdg(int nome) throws InvalidOperation, TException{
        
        int server = checkTable(nome);
        
        if(server == this.id){     
            if(checaEdg(nome)){

                int v1 = g.getArest().get(getIndexEdg(nome)).getV1();
                int v2 = g.getArest().get(getIndexEdg(nome)).getV2();

                int serverV1 = checkTable(v1);
                int serverV2 = checkTable(v2);
                
                Edge temp = g.getArest().get(getIndexEdg(nome));
                
                while(!emUsoEdg.add(nome));  
                while(!emUsoVrt.add(v1));
                if(v1 != v2)
                    while(!emUsoVrt.add(v2));
                
                if(serverV1 == this.id){ 
                    g.getVert().get(getIndexVrt(v1)).getAdja().remove(temp);
                    emUsoVrt.remove(v1);
                }else{
                    emUsoVrt.remove(v1);
                    this.transport = new TSocket("localhost",9090+serverV1);
                    this.protocol = new  TBinaryProtocol(transport);
                    this.client = new Operation.Client(protocol);      
                    transport.open();
                    client.delAdj(v1, temp);
                    transport.close();    
                }
                
                if(serverV2 == this.id){
                    g.getVert().get(getIndexVrt(v2)).getAdja().remove(g.getArest().get(getIndexEdg(nome))); 
                    emUsoVrt.remove(v2);
                }else{
                    emUsoVrt.remove(v2);
                    this.transport = new TSocket("localhost",9090+serverV2);
                    this.protocol = new  TBinaryProtocol(transport);
                    this.client = new Operation.Client(protocol);      
                    transport.open();
                    client.delAdj(v2, temp);
                    transport.close();    
                }

                // Região crítica                                       
                g.getArest().remove(getIndexEdg(nome));           
                // Região crítica
                emUsoEdg.remove(nome);
                System.out.println("delEdg("+nome+");");  
                
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui uma aresta com o nome: "+nome;
                throw io;
            }
        }else{
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            client.delEdg(nome);
            transport.close();          
        }
    }

    @Override // OK!
    public void updtEdg(int nome, int v1, int v2, double peso, String desc, boolean dir) throws InvalidOperation, TException{
        int server = checkTable(nome);
        
        if(server == this.id){         
            if(checaEdg(nome)){            
                while(!emUsoEdg.add(nome)); 
                // Região crítica
                g.getArest().get(getIndexEdg(nome)).setPeso(peso);
                g.getArest().get(getIndexEdg(nome)).setDesc(desc);
                g.getArest().get(getIndexEdg(nome)).setFlag(dir); 
                // Região crítica
                System.out.println("updtEdg("+nome+", "+v1+ ", "+v2+", "+peso+", "+desc+", "+dir+")");
                emUsoEdg.remove(nome);
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui uma aresta com o nome: "+nome;
                throw io;
            }
        }else{
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            client.updtEdg(nome, v1, v2, peso, desc, dir);
            transport.close();          
        }
    }

    @Override // OK!
    public Edge readEdg(int nome) throws InvalidOperation, TException{
        int server = checkTable(nome);
        Edge novo;
              
        if(server == this.id){  
            if(checaEdg(nome)){ 

                int v1 = g.getArest().get(getIndexEdg(nome)).getV1();
                int v2 = g.getArest().get(getIndexEdg(nome)).getV2();

                while(!emUsoEdg.add(nome));
                while(!emUsoVrt.add(v1));
                if(v1 != v2)
                    while(!emUsoVrt.add(v2));
                // Região crítica
                novo = g.getArest().get(getIndexEdg(nome)); 
                // Região crítica
                emUsoEdg.remove(nome);
                emUsoVrt.remove(v1);
                emUsoVrt.remove(v2);
                
                System.out.println("readEdg("+nome+");");
                return novo;       
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora =  Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui uma aresta com o nome: "+nome;
                throw io;
            }
        }else{
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            novo = client.readEdg(nome);
            transport.close();  
            
            return novo;
        }
    }
    
    @Override // OK!
    public List<Vertex> listVrts(int nome) throws TException{    
        List<Vertex> novo = new ArrayList<>();
        List<Vertex> temp;
        
        if(!(g.isSetVert())){
            System.out.println("listAllVrts()");
            
        }else{
           while(!emUsoVrt.isEmpty());
            // Região crítica    
            novo = g.getVert();
            // Região crítica
            emUsoVrt.clear();
            System.out.println("listAllVrts()"); 
        }
        
        if(nome != id){
            this.transport = new TSocket("localhost",9090+fingerTable[0]);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            temp = client.listVrts(nome);
            transport.close();

            if(!temp.isEmpty())
                novo.addAll(temp);
        }
       
        return novo;
    }
    
    @Override // OK!
    public List<Edge> listEdgs(int nome) throws TException{
        List<Edge> novo = new ArrayList<>();
        List<Edge> temp;
        
        if(!(g.isSetVert())){
            System.out.println("listAllEdgs()");
            
        }else{
           while(!emUsoVrt.isEmpty());
            // Região crítica    
            novo = g.getArest();
            // Região crítica
            emUsoVrt.clear();
            System.out.println("listAllEdgs()"); 
        }
        
        if(nome != id){
            this.transport = new TSocket("localhost",9090+fingerTable[0]);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            temp = client.listEdgs(nome);
            transport.close();

            if(!temp.isEmpty())
                novo.addAll(temp);
        }
       
        return novo;
    }
    
    @Override // OK!
    public List<Vertex> listAllVrts() throws TException{
        List<Vertex> novo;
        
        this.transport = new TSocket("localhost",9090+fingerTable[0]);
        this.protocol = new  TBinaryProtocol(transport);
        this.client = new Operation.Client(protocol);      
        transport.open();
        novo = client.listVrts(id);
        transport.close();
          
        return novo;
    }

    @Override // OK!
    public List<Edge> listAllEdgs() throws TException{
        List<Edge> novo = new ArrayList<>(); 
        
        this.transport = new TSocket("localhost",9090+fingerTable[0]);
        this.protocol = new  TBinaryProtocol(transport);
        this.client = new Operation.Client(protocol);      
        transport.open();
        novo = client.listEdgs(id);
        transport.close();
          
        return novo;
    }

    @Override // OK!
    public List<Edge> listVrtEdgs(int v) throws InvalidOperation, TException{
        int server = checkTable(v);
        List<Edge> novo = new ArrayList<>(); 
        if(server == this.id){
            if(checaVrt(v)){
                if(g.getVert().get(getIndexVrt(v)).isSetAdja()){
                    while(!emUsoVrt.isEmpty());
                    while(!emUsoEdg.isEmpty());
                    // Região crítica   
                    novo = g.getVert().get(getIndexVrt(v)).getAdja();
                    // Região crítica
                    emUsoVrt.clear();
                    emUsoEdg.clear();

                    System.out.println("listVrtEdgs("+v+")");

                    return novo;
                }else{
                    System.out.println("listVrtEdgs("+v+")");

                    return novo;
                }
            }else{
                InvalidOperation io = new InvalidOperation();
                LocalDateTime now = LocalDateTime.now();
                io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                io.msg = "O grafo não possui vértice com com o nome: "+v;
                throw io;
            }
        }else{
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            novo = client.listVrtEdgs(v);
            transport.close();

            System.out.println("listVrtEdgs("+v+")");

            return novo;
        }
    }

    @Override // OK!
    public List<Vertex> listVrtNgbs(int v) throws InvalidOperation, TException{ 
        int server = checkTable(v);
        List<Vertex> novo = new ArrayList<>(); 
        Vertex temp;
                        
        if(server == this.id){ 
            while(!emUsoVrt.isEmpty());
            while(!emUsoEdg.isEmpty());
            // Região crítica
                 
            if(g.getVert().get(getIndexVrt(v)).isSetAdja()){
                if(checaVrt(v)){
                    for(int i = 0; i < g.getVert().get(getIndexVrt(v)).getAdja().size(); i++){
                        System.out.println("Sextoufor");
                        if((g.getVert().get(getIndexVrt(v)).getAdja().get(i).getV1()) != v){
                            this.transport = new TSocket("localhost",9090+server);
                            this.protocol = new  TBinaryProtocol(transport);
                            this.client = new Operation.Client(protocol);      
                            transport.open();
                            temp = client.readVrt(g.getVert().get(getIndexVrt(v)).getAdja().get(i).getV1());
                            novo.add(temp);
                            transport.close();
                        }else{   
                            this.transport = new TSocket("localhost",9090+server);
                            this.protocol = new  TBinaryProtocol(transport);
                            this.client = new Operation.Client(protocol);      
                            transport.open();
                            temp = client.readVrt(g.getVert().get(getIndexVrt(v)).getAdja().get(i).getV2());
                            novo.add(temp);
                            transport.close();
                        }
                    }
                }else{
                    InvalidOperation io = new InvalidOperation();
                    LocalDateTime now = LocalDateTime.now();
                    io.hora = Integer.toString(now.getHour()) +":"+ Integer.toString(now.getMinute());
                    io.msg = "O grafo não possui vértice com com o nome: "+v;
                    throw io;
                }
            }
            // Região crítica
            emUsoVrt.clear();
            emUsoEdg.clear();       
            System.out.println("listVrtNgbs("+v+")");

            return  novo;
        }else{    
            this.transport = new TSocket("localhost",9090+server);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            novo = client.listVrtNgbs(v);
            transport.close();
            
            System.out.println("listVrtNgbs("+v+")");
            
            return novo; 
        }
    }   
        
    @Override // OK!
    public List<Edge> shrtPath(int v1, int v2) throws InvalidOperation, TException{
        List<Edge> tempEdgs = new ArrayList<>(); 
        List<Integer> vertexIndex = new ArrayList<>();
        
        Set<Integer> verificadosV = new HashSet<Integer>();
        Set<Integer> verificadosE = new HashSet<Integer>();
        Map<Integer, Integer> caminho = new HashMap<Integer, Integer>();
        Map<Integer, Edge> resposta = new HashMap<Integer, Edge>();
        Map<Integer, Double> custo = new HashMap<Integer, Double>(); 
    
        double menorCusto;
        
        Vertex tempV;
        Edge tempE; 
        int tempServer;
        boolean flag = false;
        
        custo.put(v1, (double)0);
        caminho.put(v1, v1);
        verificadosV.add(v1);
        
        vertexIndex.add(v1);
        
        while(!flag){      
        if(vertexIndex.isEmpty()){
            flag = true;
            break;
        }else{
            tempServer = checkTable(vertexIndex.get(0));
            this.transport = new TSocket("localhost",9090+tempServer);
            this.protocol = new  TBinaryProtocol(transport);
            this.client = new Operation.Client(protocol);      
            transport.open();
            tempV = client.readVrt(vertexIndex.get(0));
            transport.close();
                        
            tempEdgs.addAll(tempV.getAdja());
            
            System.out.println(tempEdgs.toString());
            
            // REMOVE AS QUE JÁ FORAM VISITADAS
            for(int j= 0; j<tempEdgs.size(); j++){
                if(verificadosE.contains(tempEdgs.get(j).getNome())){
                    tempEdgs.remove(j);
                    j--;
                }
            }
            
            System.out.println(tempEdgs.toString());
            
            tempE = tempEdgs.get(0);
            double calculo = menorCusto = 99999999.9; 
                    
            int i;
            
            // VERIFICA MENOR CUSTO
            for(i = 0; i<tempEdgs.size(); i++){
                if(custo.containsKey(tempEdgs.get(i).getV1()) && !verificadosV.contains(tempEdgs.get(i).getV2())){
                    calculo = tempEdgs.get(i).getPeso() + custo.get(tempEdgs.get(i).getV1());
                    if(menorCusto > calculo){
                        menorCusto = calculo;
                        tempE = tempEdgs.get(i);
                    }
                }
            }  
            
            verificadosV.add(vertexIndex.get(0));
            vertexIndex.remove(0);  
            
            if(custo.get(tempE.getV2()) != null){
                if(custo.get(tempE.getV2()) > menorCusto){
                    custo.put(tempE.getV2(),  menorCusto);
                    caminho.put(tempE.getV2(), tempE.getV1());
                    resposta.put(tempE.getV2(), tempE);
                }
            }else{
                custo.put(tempE.getV2(),  menorCusto);
                caminho.put(tempE.getV2(), tempE.getV1());
                resposta.put(tempE.getV2(), tempE);
            }
              
            vertexIndex.add(tempE.getV2());
            verificadosE.add(tempE.getNome());
            tempEdgs.remove(tempE);  
    
            }  
            if(verificadosV.contains(v2)){
                flag = true;
                break;
            }
            if(tempEdgs.isEmpty()){
                flag = true;
                break;
            }        
        }
  
        int teste = v2;
        tempEdgs.clear();
        
        while(teste != v1){
           tempEdgs.add(resposta.get(teste));
           teste = resposta.get(teste).getV1();
        }
        
        System.out.println("shrtPath("+v1+", "+v2+")");
        
        return tempEdgs;
    }
}

