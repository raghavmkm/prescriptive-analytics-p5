package solver.ls;

import java.io.File;
import java.io.FileNotFoundException;

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
  ArrayList<LinkedList<Integer>> routes;


  public VRPInstance(String fileName)
  {
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
    initSolution();
  }

  public void init(){
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
      routes.add(new LinkedList<Integer>());
    }
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


  public void initSolution(){
    int totalDemand = 0;
    for(int i = 0; i < numCustomers; i++){
      totalDemand += demandOfCustomer[i];
    }
    int numberDispatched = (int) Math.ceil((double)(totalDemand / (double)vehicleCapacity));
    if(numberDispatched > numVehicles){
      System.out.println("DELIVERY NOT POSSIBLE!");
      return;
    }
    System.out.println(numberDispatched);
    int[] capacities = new int[numVehicles];
    for(int i = 0; i < numVehicles; i++){
      capacities[i] = 0;
    }
    HashSet<Integer> visited = new HashSet<>();
    visited.add(0);
    HashSet<Integer> filled = new HashSet<>();

    for(int i = 0; i < numVehicles; i++){
      routes.get(i).add(0);
    }

    while(visited.size() != numCustomers){
      int flag = 1;
      for(int i = 0; i < numberDispatched; i++){
        if(filled.contains(i)){
          continue;
        }
        int currLoc = routes.get(i).getLast();
        int nextLoc = getNearestUnvisited(currLoc, visited);
        capacities[i] += demandOfCustomer[nextLoc];
        if(capacities[i] >= vehicleCapacity){
          filled.add(numberDispatched);
        }
        routes.get(i).add(nextLoc);
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
      routes.get(i).add(0);
    }
    for(int i = 0; i < numberDispatched; i++){
      for(int e : routes.get(i)){
        System.out.print(e + " ");
      }
      System.out.println();
    }
  }
}
