/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unows_client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import org.me.uno.UnoWS;
import org.me.uno.UnoWS_Service;

/**
 *
 * @author felipebrizola
 */
public class UnoWS_Client {
    
    static UnoWS port;
    
    
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        try { // Call Web Service Operation
//
//            org.me.uno.UnoWS_Service service =  new UnoWS_Service();
//            port = service.getUnoWSPort();
//            
//            String rootPath = "/Users/felipebrizola/Documents/projects/uno-ws/UnoWS_Client/src/unows_client/";
//            
//            executaTeste(rootPath + "Uno-0000",false);
//            // executaTeste("Uno-1000",false);
//            // executaTeste("Uno-3000",false);
//            
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            // TODO handle custom exceptions here
//        }
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { 
        
        String rootPath = "/home/kbase/Documentos/projects/uno-ws/UnoWS_Client/src/unows_client/";
        
        if (args.length == 0)
            args = new String[]{
                rootPath + "Uno-1000",
                rootPath + "Uno-2000",
                rootPath + "Uno-3000"
            };
        
        int numTestes = args.length;
        
        Thread[] threads = new Thread[numTestes];
        
        UnoWS_Service service =  new UnoWS_Service();
        port = service.getUnoWSPort();
        
        for (int i=0;i<numTestes;++i) {
            String r = args[i];
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        executaTeste(r, false);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                }
            };
        }
        for (int i=0;i<numTestes;++i)
            threads[i].start();
        for (int i=0;i<numTestes;++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.err.println("Falha ao esperar por teste ("+args[i]+").");
                e.printStackTrace(System.err);
            }
        }
    }

    
    private static void executaTeste(final String rad,final boolean contagem) throws IOException {
    
        try {
          String inFile = rad+".in";
          String outFile = rad + ".out";
          PrintWriter out = new PrintWriter(outFile);
          Scanner in = new Scanner(new File(inFile));
          int numOp = in.nextInt();
          for (int i=0;i<numOp;++i) {
              if (contagem)
                 System.out.print("\r"+rad+": "+(i+1)+"/"+numOp);
              int op = in.nextInt();
              String parametros = in.next();
              String param[] = parametros.split(":",-1);
              
            //   if (i == 45576)
            //       System.err.println("");
              
              switch(op) {
                  case 0:
                      System.out.println(0);
                      if (param.length!=4)
                          erro(inFile,i+1);
                      else
                          out.println(port.preRegistro(param[0],Integer.parseInt(param[1]),param[2],Integer.parseInt(param[3])));
                      break;
                  case 1:
                      System.out.println(1);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.registraJogador(param[0]));
                      break;
                  case 2:
                      System.out.println(2);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.encerraPartida(Integer.parseInt(param[0])));
                      break;
                  case 3:
                      System.out.println(3);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.temPartida(Integer.parseInt(param[0])));
                      break;
                  case 4:
                      System.out.println(4);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemOponente(Integer.parseInt(param[0])));
                      break;
                  case 5:
                      System.out.println(5);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.ehMinhaVez(Integer.parseInt(param[0])));
                      break;
                  case 6:
                      System.out.println(6);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemNumCartasBaralho(Integer.parseInt(param[0])));
                      break;
                  case 7:
                      System.out.println(7);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemNumCartas(Integer.parseInt(param[0])));
                      break;
                  case 8:
                      System.out.println(8);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemNumCartasOponente(Integer.parseInt(param[0])));
                      break;
                  case 9:
                      System.out.println(9);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.mostraMao(Integer.parseInt(param[0])));
                      break;
                  case 10:
                      System.out.println(10);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemCartaMesa(Integer.parseInt(param[0])));
                      break;
                  case 11:
                      System.out.println(11);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemCorAtiva(Integer.parseInt(param[0])));
                      break;
                  case 12:
                      System.out.println(12);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.compraCarta(Integer.parseInt(param[0])));
                      break;
                  case 13:
                      System.out.println(13);
                      if (param.length!=3)
                          erro(inFile,i+1);
                      else
                          out.println(port.jogaCarta(Integer.parseInt(param[0]),Integer.parseInt(param[1]),Integer.parseInt(param[2])));
                      break;
                  case 14:
                      System.out.println(14);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemPontos(Integer.parseInt(param[0])));
                      break;
                  case 15:
                      System.out.println(15);
                      if (param.length!=1)
                          erro(inFile,i+1);
                      else
                          out.println(port.obtemPontosOponente(Integer.parseInt(param[0])));
                      break;
                  default:
                      erro(inFile,i+1);
              }
          }
          if (contagem)
              System.out.println("... terminado!");
          else
              System.out.println(rad+": "+numOp+"/"+numOp+"... terminado!");

          out.close();
          in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }          
    }
    
    private static void erro(String arq,int operacao) {
        System.err.println("Entrada invalida: erro na operacao "+operacao+" do arquivo "+arq);
        System.exit(1);
    }
    
}
