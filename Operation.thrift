namespace java thrift

typedef i32 int

struct Vertex{
	1: int nome,
	2: int cor,
	3: double peso,
	4: string desc,
	5: list<Edge> adja,
}

struct Edge{
	1: int nome,
	2: int v1,
	3: int v2,
	4: double peso,
	5: string desc,
	6: bool flag,
}

struct Graph{
	1: list<Vertex> vert,
	2: list<Edge> arest,
}

exception InvalidOperation{
	1:string hora,
	2:string msg,
}

service Operation{
    void addVrt(1:int nome, 2:int cor, 3:double peso, 4:string desc) throws (1:InvalidOperation io),
    void delVrt(1:int nome) throws (1:InvalidOperation io),
	void updtVrt(1:int nome, 2:int cor, 3:double peso, 4:string desc) throws (1:InvalidOperation io),
    Vertex readVrt(1:int nome) throws (1:InvalidOperation io),
	void addEdg(1:int nome, 2:int v1, 3:int v2, 4:double peso, 5:string desc, 6:bool dir) throws (1:InvalidOperation io),
    void delEdg(1:int nome) throws (1:InvalidOperation io),
	void updtEdg(1:int nome, 2:int v1, 3:int v2, 4:double peso, 5:string desc, 6:bool dir) throws (1:InvalidOperation io),
    Edge readEdg(1:int nome) throws (1:InvalidOperation io),
	list<Vertex> listAllVrts(),
	list<Edge> listAllEdgs(),
	list<Edge> listVrtEdgs(1:int v) throws (1:InvalidOperation io),
	list<Vertex> listVrtNgbs(1:int v) throws (1:InvalidOperation io),
	bool checkVrt(1:int nome);
	void addAdj(1:int v, 2:Edge nome) throws (1:InvalidOperation io),
	void delAdj(1:int v, 2:Edge nome) throws (1:InvalidOperation io),
	list<Edge> shrtPath(1:int v1, 2:int v2) throws (1:InvalidOperation io),
	list<Vertex> listVrts(1:int nome),
	list<Edge> listEdgs(1:int nome),
	int checkTable(1:int key),	
}  
 	
