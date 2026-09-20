package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.example.demo.Entity.NCC;
import com.example.demo.Entity.Nhanvien;
import com.example.demo.Repository.NCCRepository;
import com.example.demo.Repository.NhanvienRepository;
import com.example.demo.Service.ReferencedRecordDeleteGuard;
import com.example.demo.Service.impl.NCCServiceImpl;
import com.example.demo.Service.impl.NhanvienServiceImpl;
import org.junit.jupiter.api.Test;

class DirectoryDeletionTest {
    @Test
    void activeSupplierCannotBePermanentlyDeleted() {
        NCCRepository repository = mock(NCCRepository.class);
        ReferencedRecordDeleteGuard guard = mock(ReferencedRecordDeleteGuard.class);
        NCC supplier = new NCC();
        supplier.setTRANGTHAI(0);
        when(repository.findForDeletion(1)).thenReturn(Optional.of(supplier));

        assertThrows(IllegalStateException.class,
                () -> new NCCServiceImpl(repository, guard).permanentlyDeleteNCC(1));
        verifyNoInteractions(guard);
        verify(repository, never()).delete(any());
    }

    @Test
    void archivedEmployeeWithReferencesIsKept() {
        NhanvienRepository repository = mock(NhanvienRepository.class);
        ReferencedRecordDeleteGuard guard = mock(ReferencedRecordDeleteGuard.class);
        Nhanvien employee = new Nhanvien();
        employee.setTRANGTHAI(1);
        when(repository.findForDeletion(2)).thenReturn(Optional.of(employee));
        doThrow(new IllegalStateException("Referenced")).when(guard).checkUnreferenced("nhanvien", 2);

        assertThrows(IllegalStateException.class,
                () -> new NhanvienServiceImpl(repository, guard).permanentlyDeleteNhanvien(2));
        verify(repository, never()).delete(any());
    }
}
