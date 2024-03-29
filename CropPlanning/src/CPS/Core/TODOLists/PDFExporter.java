/* JTableConverter.java - created: Jan 31, 2008
 * Copyright (C) 2008 Clayton Carter
 * 
 * This file is part of the project "Crop Planning Software".  For more
 * information:
 *    website: http://cropplanning.googlecode.com
 *    email:   cropplanning@gmail.com 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package CPS.Core.TODOLists;

import CPS.Data.CPSDateValidator;
import CPS.Data.CPSRecord;
import CPS.UI.Swing.CPSTable;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.util.Date;
import javax.swing.JTable;

public class PDFExporter {

    private final Font fontHeadFootItal, fontHeadFootReg, fontTableReg, fontTableHead;
//    private Font fontTableItal
    private final Font fontPageHeader;

    CPSDateValidator dateValidator;

    Document tempDoc = null;

  public PDFExporter() {
    fontHeadFootItal = FontFactory.getFont( FontFactory.HELVETICA_OBLIQUE, 8 );
    fontHeadFootReg = FontFactory.getFont( FontFactory.HELVETICA, 8 );

    fontTableReg = FontFactory.getFont( FontFactory.HELVETICA, 10 );
    fontTableHead = FontFactory.getFont( FontFactory.HELVETICA_BOLD, 10 );
//      fontTableItal = FontFactory.getFont( FontFactory.HELVETICA_OBLIQUE, 10 );

    fontPageHeader = FontFactory.getFont( FontFactory.HELVETICA_BOLD, 14 );

    dateValidator = new CPSDateValidator();

  }




    
    public void export( JTable jtable, String filename, 
                        String farmName, String docTitle, String tableTitle ) {
        
        startExport( jtable, filename, farmName, docTitle, tableTitle );
        endExport();
    }


    public void exportLandscape( JTable jtable,
                                 String filename,
                                 String farmName,
                                 String docTitle,
                                 String tableTitle ) {

        startExport( filename, farmName, docTitle, tableTitle, PageSize.LETTER.rotate() );
        addTable( jtable, tableTitle );
        endExport();
    }


    public void startExport( JTable jtable,
                            String filename,
                            String farmName,
                            String docTitle,
                            String tableTitle ) {

        startExport( filename, farmName, docTitle, tableTitle );
        addTable( jtable, tableTitle );

    }

    public void startExport( String filename,
                            String farmName,
                            String docTitle,
                            String tableTitle ) {
        startExport( filename, farmName, docTitle, tableTitle, PageSize.LETTER );
    }
    
    public void startExport( String filename,
                            String farmName,
                            String docTitle,
                            String tableTitle,
                            Rectangle pageSize ) {
        tempDoc = prepareDocument( filename, docTitle, farmName,
                                   "CropPlanning Software - http://cropplanning.googlecode.com",
                                   pageSize );
        tempDoc.open();
    }
    
    public void addTable( JTable jtable, String tableTitle ) {
        try {
          if ( tableTitle != null ) {
            tempDoc.add( new Paragraph( tableTitle, fontPageHeader ) );
          }
            tempDoc.add( new Paragraph( Chunk.NEWLINE ) ); // TODO halve the height of this
            PdfPTable t = convertJTable( jtable );
            t.setWidthPercentage( 100 ); // 100% page width
            tempDoc.add( t );
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public void addPage( JTable jtable, String tableTitle ) {
        try {
            tempDoc.newPage();
            addTable( jtable, tableTitle );
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public void endExport() {
        tempDoc.close();
    }
    
    
    
    private Document prepareDocument( String filename, 
                                      final String title,
                                      final String author,
                                      final String creator,
                                      final Rectangle pageSize ) {
        
        System.out.println( "DEBUG(PDFExporter): Creating document: " + filename );
        
        Document d = new Document();

        d.setPageSize( pageSize );
        // TODO alter page orientation?  maybe useful for seed order worksheet

        d.addTitle( title );
        d.addAuthor( author );
//        d.addSubject( );
//        d.addKeywords( );
        d.addCreator( creator );
        
        // left, right, top, bottom - scale in points (~72 points/inch)
        d.setMargins( 35, 35, 35, 44 );
        
        try {
            PdfWriter writer = PdfWriter.getInstance( d, new FileOutputStream( filename ) );
            // add header and footer
            writer.setPageEvent( new PdfPageEventHelper() {
                 public void onEndPage( PdfWriter writer, Document document ) {
                    try {
                        Rectangle page = document.getPageSize();
                        
                        PdfPTable head = new PdfPTable( 3 );
                        head.getDefaultCell().setBorderWidth(0);
                        head.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_LEFT );
                        head.addCell( new Phrase( author, fontHeadFootItal ) );
                        
                        head.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_CENTER );
                        head.addCell( new Phrase( title, fontHeadFootReg ));
                        
                        head.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_RIGHT );
                        head.addCell( "" );
                        
                        head.setTotalWidth( page.getWidth() - document.leftMargin() - document.rightMargin() );
                        head.writeSelectedRows( 0, -1, document.leftMargin(), page.getHeight() - document.topMargin() + head.getTotalHeight(),
                                                writer.getDirectContent() );
                        
                        PdfPTable foot = new PdfPTable( 3 );
                        
                        foot.getDefaultCell().setBorderWidth(0);
                        foot.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_LEFT );
                        foot.addCell( new Phrase( creator, fontHeadFootItal ) );
                        
                        foot.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_CENTER );
                        foot.addCell("");
                        
                        foot.getDefaultCell().setHorizontalAlignment( PdfPCell.ALIGN_RIGHT );
                        foot.addCell( new Phrase( "Page " + document.getPageNumber(), fontHeadFootReg ));
                        
                        
                        foot.setTotalWidth( page.getWidth() - document.leftMargin() - document.rightMargin() );
                        foot.writeSelectedRows( 0, -1, document.leftMargin(), document.bottomMargin(),
                                                writer.getDirectContent() );
                    } catch ( Exception e ) {
                        throw new ExceptionConverter( e );
                    }
                }
            } );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return d;
    }
    
    /**
     * @param args the command line arguments
     */
    public PdfPTable convertJTable( JTable jtable ) {

       boolean tableIncludesNotes = false;
       boolean rowHasNotes = false;
       String notesValue = "";
       int notesIndex = -1;
       
       // find Notes column (if there is one)
       for ( int col = 0; col < jtable.getColumnCount(); col++ ) {
          String headName;
          if ( jtable instanceof CPSTable )
             headName = jtable.getColumnModel().getColumn( col ).getHeaderValue().toString();
          else
             headName = jtable.getColumnName( col );
          if ( headName.equalsIgnoreCase( "Planting Notes" ) ) {
             tableIncludesNotes = true;
             notesIndex = col;
          }
       }

       int colCount = (tableIncludesNotes) ? jtable.getColumnCount() - 1 : jtable.getColumnCount();
       PdfPTable table = new PdfPTable( colCount );

        // create header row
        for ( int col = 0; col < jtable.getColumnCount(); col++ ) {
           String headName;
            if ( jtable instanceof CPSTable )
                headName = jtable.getColumnModel().getColumn(col).getHeaderValue().toString();
            else
                headName = jtable.getColumnName( col );
           if ( ! tableIncludesNotes || col != notesIndex ) {
             HeadCell hc = new HeadCell( headName );

             if ( jtable.getColumnClass( col ).equals( Boolean.TRUE.getClass() ) ||
                  jtable.getColumnClass( col ).equals( new Integer( 0 ).getClass() ) ||
                  jtable.getColumnClass( col ).equals( new Double( 0 ).getClass() ) ||
                  jtable.getColumnClass( col ).equals( new Float( 0 ).getClass() ) ) {
               hc.setRotation(90);
               hc.setFixedHeight( 60f );
             }

             table.addCell( hc );
           }
        }
        table.setHeaderRows( 1 );
        
        // now fill in the rest of the table
        for ( int row = 0; row < jtable.getRowCount(); row++ ) {
           rowHasNotes = false;
           
           for ( int col = 0; col < jtable.getColumnCount(); col++ ) {
                Object o = jtable.getValueAt( row, col );

                if ( o == null ) {
                   if ( ! tableIncludesNotes || col != notesIndex )
                    table.addCell( new RegCell( "" ) );
                }

                else if ( o instanceof Date )
                    table.addCell( new RegCell( CPSDateValidator.format( (Date) o,
                                                                         CPSDateValidator.DATE_FORMAT_SHORT_DAY_OF_WEEK )));

                else if ( o instanceof Boolean )
                    if ( ( (Boolean) o ).booleanValue() )
                        table.addCell( new CenterCell( "X" ) );
                    else
                        table.addCell( new RegCell( "" ) );

                else if ( o instanceof Float )
                    table.addCell( new RegCell( CPSRecord.formatFloat( ((Float) o).floatValue(), 3) ));

                else if ( o instanceof Double )
                    table.addCell( new RegCell( CPSRecord.formatFloat( ((Double) o).floatValue(), 3) ));

                else {
                   
                   if ( tableIncludesNotes && col == notesIndex ) {
                      if ( o == null || o.equals(""))
                         rowHasNotes = false;
                      else {
                         rowHasNotes = true;
                         notesValue = o.toString();
                      }
                   }
                   else
                      table.addCell( new RegCell( o.toString() ) );
                }
            }
           
           // now deal w/ the Notes data
           if ( tableIncludesNotes && rowHasNotes ) {
              table.addCell( new NoteHeadCell() );
              NoteCell c = new NoteCell( notesValue );
              // reset the font to be smaller
              c.setPhrase( new Phrase( notesValue, fontHeadFootReg ));
              c.setColspan( colCount - 1 );
              table.addCell( c );
           }
        }
            
        // set the widths for the columns
        float[] widths = new float[colCount];
        for ( int col = 0; col < colCount; col++ ) {
           if ( tableIncludesNotes && col == notesIndex )
              continue;
           else if ( jtable.getColumnClass( col ).equals( new Boolean( true ).getClass() ) )
              widths[col] = 2.25f;
           else if ( jtable.getColumnClass( col ).equals( new Integer( 0 ).getClass() ) ||
                     jtable.getColumnClass( col ).equals( new Double( 0 ).getClass() ) ||
                     jtable.getColumnClass( col ).equals( new Float( 0 ).getClass() ) )
              widths[col] = 5f;
           else // String, Date, etc
              widths[col] = 10f;
        }
        
        try {
            table.setWidths( widths );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        
        return table;
    }

    
    public class RegCell extends PdfPCell {
        
        public RegCell( String s ) {
            super( new Phrase( s, fontTableReg ));
            setBackgroundColor( Color.WHITE );
            setHorizontalAlignment( PdfPCell.ALIGN_LEFT );
            setBorderWidth( .25f );
        }
    }

    public class CenterCell extends RegCell {

        public CenterCell( String s ) {
            super( s );
            setHorizontalAlignment( PdfPCell.ALIGN_CENTER );
        }
    }

    public class HeadCell extends PdfPCell {
        
        public HeadCell( String s ) {
            super( new Phrase( s, fontTableHead ));
            setBackgroundColor( Color.LIGHT_GRAY );
            setHorizontalAlignment( PdfPCell.ALIGN_CENTER );
            setVerticalAlignment( PdfPCell.ALIGN_MIDDLE );
        }
    }
    
    public class NoteHeadCell extends  PdfPCell {
       public NoteHeadCell() {
            super( new Phrase( "Notes:", fontHeadFootItal ));
            setBackgroundColor( Color.WHITE );
            setHorizontalAlignment( PdfPCell.ALIGN_RIGHT );
            disableBorderSide( Rectangle.LEFT );
            disableBorderSide( Rectangle.RIGHT );
            setBorderWidth( .25f );
        }
    }

    public class NoteCell extends RegCell {

      public NoteCell(String s) {
        super(s);
        disableBorderSide( Rectangle.LEFT );
        disableBorderSide( Rectangle.RIGHT );
        setMinimumHeight( 20f );
      }

    }
    
}
