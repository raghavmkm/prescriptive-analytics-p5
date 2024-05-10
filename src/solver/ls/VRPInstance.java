package solver.ls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class VRPInstance
{
  // VRP Input Parameters
  int numCustomers;        		// the number of customers	   
  int numVehicles;           	// the number of vehicles
  int vehicleCapacity;			// the capacity of the vehicles
  int[] demandOfCustomer;		// the demand of each customer
  double[] xCoordOfCustomer;	// the x coordinate of each customer
  double[] yCoordOfCustomer;	// the y coordinate of each customer
  
  //variables created by me
  double[][] distanceMatrix;
  ArrayList<LinkedList<Edge>> bestRoute;
  double bestCost;
  ArrayList<LinkedList<Edge>> routes;
  double currCost;
  int[] capacities;
  LinkedList<Edge> edgeList;
  String NameOfFile;

  public VRPInstance(String fileName)
  {
    NameOfFile = fileName;
    Scanner read = null;
    try
    {
      read = new Scanner(new File(fileName));
    } catch (FileNotFoundException e)
    {
      System.out.println("Error: in VRPInstance() " + fileName + "\n" + e.getMessage());
      System.exit(-1);
    }

    numCustomers = read.nextInt(); 
    numVehicles = read.nextInt();
    vehicleCapacity = read.nextInt();
    
    System.out.println("Number of customers: " + numCustomers);
    System.out.println("Number of vehicles: " + numVehicles);
    System.out.println("Vehicle capacity: " + vehicleCapacity);
      
    demandOfCustomer = new int[numCustomers];
	  xCoordOfCustomer = new double[numCustomers];
	  yCoordOfCustomer = new double[numCustomers];
	
    for (int i = 0; i < numCustomers; i++)
	  {
		  demandOfCustomer[i] = read.nextInt();
		  xCoordOfCustomer[i] = read.nextDouble();
		  yCoordOfCustomer[i] = read.nextDouble();
	  }
	
	  for (int i = 0; i < numCustomers; i++)
		  System.out.println(demandOfCustomer[i] + " " + xCoordOfCustomer[i] + " " + yCoordOfCustomer[i]);
    
    init();
    init_greedy_solution();
    // print_routes();;
    // print_capacities();
    //System.out.println(get_cost());
    simulatedAnnealing();
    // checkSolution();
    // print_best_routes();
    // print_capacities();
    // System.out.println(bestCost);
    // createSolutionFile();
  }

  public double get_cost(){
    double cost = 0;
    for(int i = 0; i < numVehicles; i++){
      for(Edge e : routes.get(i)){
        cost += distanceMatrix[e.start][e.end];
      }
    }
    return cost;
  }

  public void init(){
    edgeList = new LinkedList<Edge>();
    distanceMatrix = new double[numCustomers][numCustomers];
    for(int i = 0; i < numCustomers; i++){
        for(int j = 0; j < numCustomers; j++){
            double x2 = Math.pow((xCoordOfCustomer[i] - xCoordOfCustomer[j]), 2);
            double y2 = Math.pow((yCoordOfCustomer[i] - yCoordOfCustomer[j]), 2);
            distanceMatrix[i][j] = Math.sqrt(x2 + y2);
        }
    }
    routes = new ArrayList<>(numVehicles);
    for(int i = 0; i < numVehicles; i++){
      routes.add(new LinkedList<Edge>());
    }
    capacities = new int[numVehicles];
    for(int i = 0; i < numVehicles; i++){
      capacities[i] = 0;
    }
  }

  public void simulatedAnnealing(){
    double temperature = currCost;
    int iteration = 0;
    //termination condition is number of iterations
    while(iteration < 4096000){
      int[] neighbor = getNeighbour();
      ArrayList<LinkedList<Edge>> neighborRoute = new ArrayList<>();
      for(int i = 0; i < numVehicles; i++){
        neighborRoute.add(new LinkedList<Edge>());
        for(Edge e : routes.get(i)){
          Edge newEdge = new Edge(e.start);
          newEdge.setEnd(e.end);
          neighborRoute.get(i).add(newEdge);
        }
      }
      if(neighbor[4] == 1){
        for(Edge e : neighborRoute.get(neighbor[0])){
          if(e.end == neighbor[2]){
            e.end = neighbor[3];
          }
          if(e.start == neighbor[2]){
            e.start = neighbor[3];
          }
        }
        for(Edge e : neighborRoute.get(neighbor[1])){
          if(e.end == neighbor[3]){
            e.end = neighbor[2];
          }
          if(e.start == neighbor[3]){
            e.start = neighbor[2];
          }
        }
      }
      else if(neighbor[4] == 2){
        LinkedList<Edge> v1 = neighborRoute.get(neighbor[0]);
        for(int i = 0; i < v1.size(); i++){
          if(v1.get(i).end == neighbor[2]){
            int next = v1.get(i + 1).end;
            v1.get(i).end = next;
            v1.remove(i + 1);
          }
        }
        LinkedList<Edge> v2 = neighborRoute.get(neighbor[1]);
        for(int i = 0; i < v2.size(); i++){
          if(v2.get(i).start == neighbor[3]){
            int next = v2.get(i).end;
            v2.get(i).end = neighbor[2];
            Edge e = new Edge(neighbor[2]);
            e.setEnd(next);
            v2.add(i + 1, e);
          }
        }
      }
      else{
        LinkedList<Edge> v1 = neighborRoute.get(neighbor[0]);
        for(int i = 0; i < v1.size(); i++){
          if(v1.get(i).end == neighbor[2]){
            int next = v1.get(i + 1).end;
            v1.get(i).end = next;
            v1.remove(i + 1);
          }
        }
        for(int i = 0; i < v1.size(); i++){
          if(v1.get(i).start == neighbor[3]){
            int next = v1.get(i).end;
            v1.get(i).end = neighbor[2];
            Edge e = new Edge(neighbor[2]);
            e.setEnd(next);
            v1.add(i + 1, e);
          }
        }
      }
      double neighborCost = 0;
      for(int i = 0; i < numVehicles; i++){
        for(Edge e : neighborRoute.get(i)){
          neighborCost += distanceMatrix[e.start][e.end];
        }
      }
      double delta = neighborCost - currCost;
      if(delta < 0 || Math.random() < Math.exp((-1 * delta) / temperature)){
        routes = neighborRoute;
        currCost = neighborCost;
        if(neighbor[4] == 1){
          capacities[neighbor[0]] += demandOfCustomer[neighbor[3]] - demandOfCustomer[neighbor[2]];
          capacities[neighbor[1]] += demandOfCustomer[neighbor[2]] - demandOfCustomer[neighbor[3]];
        }
        else if(neighbor[4] == 2){
          capacities[neighbor[0]] -= demandOfCustomer[neighbor[2]];
          capacities[neighbor[1]] += demandOfCustomer[neighbor[2]];
        }
      }

      if(currCost < bestCost){
        bestRoute = routes;
        bestCost = currCost;
      }
      if(iteration % 512 == 0){
        temperature = temperature * 0.99;
      }
      iteration++;
    }
  }

  public int[] getNeighbour(){
    int[] result = new int[5];
    while(true){
      int tmp = (int) (Math.random() * 3 + 1);
      if(tmp == 1){
        //swapping
        int v1 = (int) (Math.random() * numVehicles + 0);
        while(routes.get(v1).size() < 2){
          v1 = (int) (Math.random() * numVehicles + 0);
        }
        int v2 = (int) (Math.random() * numVehicles + 0);
        while(routes.get(v2).size() < 2){
          v2 = (int) (Math.random() * numVehicles + 0);
        }
        while(v2 == v1){
          v2 = (int) (Math.random() * numVehicles + 0);
          while(routes.get(v2).size() < 2){
            v2 = (int) (Math.random() * numVehicles + 0);
          }
        }
        int r1 = routes.get(v1).size() - 1;
        int r2 = routes.get(v2).size() - 1;
        int i1 = (int) (Math.random() * r1 + 0);
        int i2 = (int) (Math.random() * r2 + 0);
        int c1 = routes.get(v1).get(i1).end;
        int c2 = routes.get(v2).get(i2).end;
        if((capacities[v1] - demandOfCustomer[c1] + demandOfCustomer[c2] <= vehicleCapacity) && capacities[v2] - demandOfCustomer[c2] + demandOfCustomer[c1] <= vehicleCapacity){
          result[0] = v1;
          result[1] = v2;
          result[2] = c1;
          result[3] = c2;
          result[4] = 1;
          return result;
        }
        else{
          continue;
        }
      }
      else if(tmp == 2){
        //inserting and removing
        int v1 = (int) (Math.random() * numVehicles + 0);
        while(routes.get(v1).size() < 2){
          v1 = (int) (Math.random() * numVehicles + 0);
        }
        int v2 = (int) (Math.random() * numVehicles + 0);
        while(v2 == v1){
          v2 = (int) (Math.random() * numVehicles + 0);
        }
        int r1 = routes.get(v1).size() - 1;
        int r2 = routes.get(v2).size();
        int i1 = (int) (Math.random() * r1 + 0);
        int i2 = (int) (Math.random() * r2 + 0);
        int c1 = routes.get(v1).get(i1).end;
        int c2 = routes.get(v2).get(i2).start;
        if(capacities[v2] + demandOfCustomer[c1] <= vehicleCapacity){
          result[0] = v1;
          result[1] = v2;
          result[2] = c1;
          result[3] = c2;
          result[4] = 2;
          return result;
        }
        else{
          continue;
        }
      }
      else{
        //reorder
        int v1 = (int) (Math.random() * numVehicles + 0);
        if(routes.get(v1).size() < 4){
          continue;
        }
        int r1 = routes.get(v1).size() - 1;
        int i1 = (int) (Math.random() * r1 + 0);
        int c1 = routes.get(v1).get(i1).end;
        int c2 = routes.get(v1).get(i1).start;
        int r2 = routes.get(v1).size();
        int i2 = (int) (Math.random() * r2 + 0);
        int pos = routes.get(v1).get(i2).start;
        while(pos == c1 || pos == c2){
          i2 = (int) (Math.random() * r2 + 0);
          pos = routes.get(v1).get(i2).start;
        } 
        result[0] = v1;
        result[1] = v1;
        result[2] = c1;
        result[3] = pos;
        result[4] = 3;
        return result;
      }
    }
  }

  public int get_highest_demand(HashSet<Integer> serviced){
    int index = -1;
    int maxVal = Integer.MIN_VALUE;
    for(int i = 0; i < numCustomers; i++){
      if(!serviced.contains(i)){
        if(demandOfCustomer[i] > maxVal){
          index = i;
          maxVal = demandOfCustomer[i];
        }
      }
    }
    if(index != -1){
      return index;
    }
    else{
      System.out.println("Error in customer dispatch");
      return -1;
    }
  }

  public void init_greedy_solution(){
    HashSet<Integer> serviced = new HashSet<>();
    serviced.add(0);
    for(int i = 0; i < numVehicles; i++){
          Edge e = new Edge(0);
          edgeList.add(e);
          routes.get(i).add(e);
        }
    while(serviced.size() != numCustomers){
      int currDemand = get_highest_demand(serviced);
      int flag = 0;
      for(int i = 0; i < numVehicles; i++){
        if(capacities[i] + demandOfCustomer[currDemand] <= vehicleCapacity){
          capacities[i] += demandOfCustomer[currDemand];
          routes.get(i).getLast().setEnd(currDemand);
          Edge e = new Edge(currDemand);
          edgeList.add(e);
          routes.get(i).add(e);
          serviced.add(currDemand);
          flag = 1;
          break;
        }
      }
      if(flag == 0){
        System.out.println("INFEASIBLE");
        return;
      }
    }
    for(int i = 0; i < numVehicles; i++){
      routes.get(i).getLast().setEnd(0);
    }
    bestRoute = routes;
    currCost = get_cost();
    bestCost = currCost;
  }

  public int getNearestUnvisited(int location, HashSet<Integer> visited){
    int next = -1;
    double minDistance = Double.MAX_VALUE;
    for(int i = 0; i < numCustomers; i++){
      if(i != location && !visited.contains(i)){
        if(distanceMatrix[location][i] < minDistance){
          next = i;
          minDistance = distanceMatrix[location][i];
        }
      }
    }
    return next;
  }


  public void initSolutionOpt(){
    int totalDemand = 0;
    for(int i = 0; i < numCustomers; i++){
      totalDemand += demandOfCustomer[i];
    }
    int numberDispatched = (int) Math.ceil((double)(totalDemand / (double)vehicleCapacity));
    if(numberDispatched > numVehicles){
      System.out.println("DELIVERY NOT POSSIBLE!");
      return;
    }
    HashSet<Integer> visited = new HashSet<>();
    visited.add(0);
    HashSet<Integer> filled = new HashSet<>();

    for(int i = 0; i < numVehicles; i++){
      routes.get(i).add(new Edge(0));
    }

    while(visited.size() != numCustomers){
      int flag = 1;
      for(int i = 0; i < numberDispatched; i++){
        if(filled.contains(i)){
          continue;
        }
        int currLoc = routes.get(i).getLast().start;
        int nextLoc = getNearestUnvisited(currLoc, visited);
        capacities[i] += demandOfCustomer[nextLoc];
        if(capacities[i] >= vehicleCapacity){
          filled.add(i);
        }
        routes.get(i).getLast().setEnd(nextLoc);
        routes.get(i).add(new Edge(nextLoc));
        visited.add(nextLoc);
        flag = 0;
        if(visited.size() == numCustomers){
          break;
        }
      }
      if(flag == 1){
        if(numberDispatched < numVehicles){
          numberDispatched += 1;
        }
        else{
          System.out.println("infeasible solution");
          break;
        }
      }
    }
    for(int i = 0; i < numberDispatched; i++){
      routes.get(i).getLast().setEnd(0);
    }
  }


  public void checkSolution(){
    for(int i = 0; i < numVehicles; i++){
      int totalCapacity = 0;
      for(Edge e : bestRoute.get(i)){
        totalCapacity += demandOfCustomer[e.start];
      }
      if(totalCapacity > vehicleCapacity){
        System.out.println("FAILS CHECK 1");
        return;
      }
    }
    HashSet<Integer> customersCaptured = new HashSet<>();
    customersCaptured.add(0);
    for(int i = 0; i < numVehicles; i++){
      for(Edge e : bestRoute.get(i)){
        if(e.start == 0){
          continue;
        }
        else{
          if(customersCaptured.contains(e.start)){
            System.out.println("FAILS CHECK 2");
            return;
          }
          else{
            customersCaptured.add(e.start);
          }
        }
      }
    }
    if(customersCaptured.size() != numCustomers){
      System.out.println("FAILS CHECK 3");
      return;
    }
    for(int i = 0; i < numVehicles; i++){
      if(bestRoute.get(i).getFirst().start != 0 || bestRoute.get(i).getLast().end != 0){
        System.out.println("FAILS CHECK 4");
        return;
      }
      int zeroCount = 0;
      for(Edge e : bestRoute.get(i)){
        if(e.start == 0){
          zeroCount += 1;
        }
        if(e.end == 0){
          zeroCount += 1;
        }
      }
      if(zeroCount != 2){
        System.out.println("FAILS CHECK 5");
        return;
      }
    }
    System.out.println("PASSES CHECKER!");
  }



  public void print_routes(){
    for(int i = 0; i < numVehicles; i++){
          for(Edge e : routes.get(i)){
            System.out.print(e.start + " ");
          }
          System.out.println(0);
        }
  }

  public void print_best_routes(){
    for(int i = 0; i < numVehicles; i++){
          for(Edge e : bestRoute.get(i)){
            System.out.print(e.start + " ");
          }
          System.out.println(0);
        }
  }


  public void print_capacities(){
    for(Integer e : capacities){
      System.out.print(e + " ");
    }
    System.out.println();
  }

  public String getBestSolutionString(){
    String output = "";
    output += "0";
    for(int i = 0; i < numVehicles; i++){
      for(Edge e : bestRoute.get(i)){
        output += " ";
        output += e.start; 
      }
      output += " ";
      output += 0;
    }
    return output;
  }

  public void createSolutionFile(){
    
    File file = new File("output/" + "sol_" + NameOfFile.substring(6));
    try{
      FileWriter writer = new FileWriter(file);
      writer.write(bestCost + " " + 0 + "\n");
      for(int i = 0; i < numVehicles; i++){
        for(Edge e : bestRoute.get(i)){
          writer.write(e.start + " ");
        }
        writer.write(0 + "\n");
      }
      writer.close();
    }
    catch (IOException e){
      System.out.println("Write has failed!");
      e.printStackTrace();
    }
  }
}
