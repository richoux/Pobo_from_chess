//
// Created by flo on 29/09/2023.
//

#ifndef HELPERS_HPP
#define HELPERS_HPP

#include <jni.h>
#include <vector>
#include "lib/include/ghost/variable.hpp"

enum Direction { TOPRIGHT, RIGHT, BOTTOMRIGHT, BOTTOM };
enum PieceType { PO, BO, WHATEVER };

struct Position
{
	int row;
	int column;

	Position(int row, int col)
	: row(row),
	  column(col)
	{ }
};

bool check_three_in_a_row( int from_row,
													 int from_col,
													 Direction direction,
													 PieceType type,
													 jbyte * const simulation_grid );

bool check_two_in_a_row( int from_row,
												 int from_col,
												 Direction direction,
												 PieceType Type,
												 jbyte * const simulation_grid );

bool is_two_in_a_row_in_corner( int from_row,
																int from_col,
																Direction direction );

bool is_two_in_a_row_blocked( int from_row,
															int from_col,
															Direction direction,
															jbyte * const simulation_grid );

int	count_Po_in_a_row( int from_row,
												int from_col,
												Direction direction,
												jbyte * const simulation_grid );

int get_next_row( int from_row,
									Direction direction );

int get_next_col( int from_col,
									Direction direction );

Position get_position_toward( const Position &position, Direction direction );
Position get_position_toward( const Position &position, int direction );
Position get_previous_position( const Position &position, Direction direction );
Position get_previous_position( const Position &position, int direction );

bool is_valid_position( const Position &position );
bool is_valid_position( int row, int col );
bool is_empty_position( jbyte * const simulation_grid, int row, int col );
bool is_blue_piece_on( jbyte * const simulation_grid, int row, int col );
bool is_empty_position( jbyte * const simulation_grid, const Position &position );
bool is_blue_piece_on( jbyte * const simulation_grid, const Position &position );
bool is_fully_on_border( const std::vector<Position> &group );
bool is_partially_on_border( const std::vector<Position> &group );
bool is_on_border( int from_row,
                   int from_col,
                   Direction direction,
                   int length,
                   bool fully );
bool is_in_center( const Position &position );
bool next_to_other_own_pieces( jbyte *const simulation_grid, const Position &position );
bool is_blocking( jbyte * const simulation_grid, int row, int col );
bool is_blocking( jbyte * const simulation_grid, const Position &position );
bool is_two_unblocked_bo_and_one_po( int from_row,
                                     int from_col,
                                     Direction direction,
                                     jbyte * const simulation_grid );

std::vector< std::vector<Position> > get_promotions( jbyte * const simulation_grid,
																											jboolean blue_turn,
																											jint blue_pool_size,
																											jint red_pool_size );

#endif //HELPERS_HPP
